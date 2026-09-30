package com.biliplus.service.Impl;

import com.biliplus.constant.Notify;
import com.biliplus.constant.ReportTarget;
import com.biliplus.constant.VideoStatus;
import com.biliplus.exception.BusinessException;
import com.biliplus.mapper.AdminVideoMapper;
import com.biliplus.mapper.CommentMapper;
import com.biliplus.mapper.ReportMapper;
import com.biliplus.pojo.entity.Comment;
import com.biliplus.pojo.entity.Report;
import com.biliplus.result.PageResult;
import com.biliplus.service.NotificationService;
import com.biliplus.service.ReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private ReportMapper reportMapper;

    @Autowired
    private AdminVideoMapper adminVideoMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private com.biliplus.mapper.DanmakuMapper danmakuMapper;

    @Autowired
    private com.biliplus.service.LiveRoomService liveRoomService;

    @Autowired
    private com.biliplus.service.UserPenaltyService userPenaltyService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private com.biliplus.service.UserCreditService userCreditService;

    @Autowired
    private com.biliplus.service.EventLogService eventLogService;

    @Autowired
    private com.biliplus.service.AdminOperationLogService adminOperationLogService;

    @Override
    @Transactional
    public Report submit(Long reporterId, Integer targetType, Long targetId, Integer reason, String detail) {
        if (reporterId == null) {
            throw new BusinessException("请先登录");
        }
        if (targetType == null || targetId == null) {
            throw new BusinessException("举报对象不能为空");
        }
        if (reason == null || reason < 1 || reason > 5) {
            throw new BusinessException("请选择举报原因");
        }
        if (targetType < 1 || targetType > 5) {
            throw new BusinessException("举报类型不合法");
        }
        String trimmedDetail = detail == null ? null : detail.trim();
        if (trimmedDetail != null && trimmedDetail.length() > 500) {
            throw new BusinessException("补充说明不能超过500字");
        }
        // 同一目标未结案时不重复受理，避免刷举报
        if (reportMapper.countPending(reporterId, targetType, targetId) > 0) {
            throw new BusinessException("你已举报过该内容，正在处理中");
        }

        Report report = new Report();
        report.setReporterId(reporterId);
        report.setTargetType(targetType);
        report.setTargetId(targetId);
        report.setReason(reason);
        report.setDetail(trimmedDetail);
        report.setCreateTime(LocalDateTime.now());
        reportMapper.insert(report);
        log.info("用户 {} 举报 targetType={}, targetId={}, reason={}",
                reporterId, targetType, targetId, reason);
        eventLogService.record(com.biliplus.constant.EventType.REPORT, reporterId,
                "target_type_" + targetType, targetId,
                null, "{\"reason\":" + reason + "}", com.biliplus.constant.EventType.SOURCE_WEB);
        return report;
    }

    @Override
    public PageResult adminList(Integer status, Integer targetType, Integer page, Integer size) {
        int p = page == null || page < 1 ? 1 : page;
        int s = size == null || size < 1 ? 20 : Math.min(size, 100);
        List<Report> records = reportMapper.adminList(status, targetType, (p - 1) * s, s);
        long total = reportMapper.adminCount(status, targetType);
        return new PageResult(total, records);
    }

    @Override
    @Transactional
    public void handle(Long adminId, Long reportId, Integer status, String remark) {
        if (reportId == null) {
            throw new BusinessException("举报ID不能为空");
        }
        if (status == null
                || (status != ReportTarget.STATUS_HANDLED && status != ReportTarget.STATUS_REJECTED)) {
            throw new BusinessException("处理结果只能是「成立」或「驳回」");
        }
        Report report = reportMapper.selectById(reportId);
        if (report == null) {
            throw new BusinessException("举报不存在");
        }
        if (report.getStatus() != null && report.getStatus() != ReportTarget.STATUS_PENDING) {
            throw new BusinessException("该举报已处理");
        }

        String trimmedRemark = StringUtils.hasText(remark) ? remark.trim() : null;
        if (trimmedRemark != null && trimmedRemark.length() > 255) {
            throw new BusinessException("处理备注不能超过255字");
        }

        int rows = reportMapper.handle(reportId, status, adminId, trimmedRemark, LocalDateTime.now());
        if (rows <= 0) {
            throw new BusinessException("该举报已处理");
        }

        if (status == ReportTarget.STATUS_HANDLED) {
            applyTakedown(report);
        }
        log.info("管理员 {} 处理举报 {}，结果 status={}", adminId, reportId, status);
        adminOperationLogService.record("report.handle", "report", reportId,
                "status=" + status + (trimmedRemark == null ? "" : ", remark=" + trimmedRemark));
    }

    /** 举报成立后的联动处置：视频下架 / 评论·弹幕删除 / 用户禁言 / 直播间强制下播 */
    private void applyTakedown(Report report) {
        Integer type = report.getTargetType();
        Long targetId = report.getTargetId();
        if (type == null || targetId == null) {
            return;
        }
        if (type == ReportTarget.TYPE_VIDEO) {
            int rows = adminVideoMapper.updateStatus(targetId, VideoStatus.NORMAL, VideoStatus.OFFLINE);
            if (rows > 0) {
                com.biliplus.pojo.entity.Video video = adminVideoMapper.selectById(targetId);
                if (video != null && video.getUserId() != null) {
                    notificationService.notify(video.getUserId(), null, Notify.TYPE_AUDIT,
                            "你的稿件因举报被下架",
                            StringUtils.hasText(report.getHandleRemark())
                                    ? report.getHandleRemark() : "内容违反社区规范，已被下架。",
                            Notify.BIZ_VIDEO, targetId);
                    // 举报成立：对内容作者计一次违规
                    userCreditService.applyViolation(video.getUserId(), -10, "稿件被举报并下架");
                }
            }
        } else if (type == ReportTarget.TYPE_COMMENT) {
            Comment comment = commentMapper.selectById(targetId);
            // 复用评论软删：where 带 user_id，用评论作者本人身份触发
            if (comment != null && comment.getUserId() != null) {
                commentMapper.softDelete(targetId, comment.getUserId());
                userCreditService.applyViolation(comment.getUserId(), -10, "评论被举报并删除");
                notificationService.notify(comment.getUserId(), null, Notify.TYPE_AUDIT,
                        "你的评论因举报被删除",
                        StringUtils.hasText(report.getHandleRemark())
                                ? report.getHandleRemark() : "内容违反社区规范，已被删除。",
                        Notify.BIZ_VIDEO, comment.getVideoId());
            }
        } else if (type == ReportTarget.TYPE_DANMAKU) {
            com.biliplus.pojo.entity.Danmaku danmaku = danmakuMapper.selectById(targetId);
            if (danmaku != null) {
                danmakuMapper.softDelete(targetId);
                if (danmaku.getUserId() != null) {
                    userCreditService.applyViolation(danmaku.getUserId(), -10, "弹幕被举报并删除");
                    notificationService.notify(danmaku.getUserId(), null, Notify.TYPE_AUDIT,
                            "你的弹幕因举报被删除",
                            StringUtils.hasText(report.getHandleRemark())
                                    ? report.getHandleRemark() : "内容违反社区规范，已被删除。",
                            Notify.BIZ_VIDEO, danmaku.getVideoId());
                }
            }
        } else if (type == ReportTarget.TYPE_USER) {
            // 用户举报成立：扣信用分 + 自动禁言 3 天
            userCreditService.applyViolation(targetId, -10, "用户被举报并核实违规");
            try {
                userPenaltyService.penalize(targetId, "mute",
                        StringUtils.hasText(report.getHandleRemark())
                                ? report.getHandleRemark() : "举报核实违规，禁言 3 天",
                        3, null);
            } catch (Exception e) {
                // 已有生效中的处置时不重复
                log.warn("举报成立自动禁言未执行 userId={}", targetId, e);
            }
            notificationService.notify(targetId, null, Notify.TYPE_AUDIT,
                    "你的账号因举报被禁言",
                    StringUtils.hasText(report.getHandleRemark())
                            ? report.getHandleRemark() : "内容违反社区规范，账号已被临时禁言。",
                    null, null);
        } else if (type == ReportTarget.TYPE_LIVE_ROOM) {
            try {
                liveRoomService.forceStop(targetId);
            } catch (Exception e) {
                log.warn("举报成立强制下播未执行 roomId={}", targetId, e);
            }
            com.biliplus.pojo.entity.LiveRoom room = liveRoomService.getLiveRoom(targetId);
            if (room != null && room.getUserId() != null) {
                userCreditService.applyViolation(room.getUserId(), -10, "直播间被举报并强制下播");
                notificationService.notify(room.getUserId(), null, Notify.TYPE_LIVE_START,
                        "你的直播间因举报被强制下播",
                        StringUtils.hasText(report.getHandleRemark())
                                ? report.getHandleRemark() : "直播间违反社区规范，已被强制下播。",
                        "live", targetId);
            }
        }
    }
}
