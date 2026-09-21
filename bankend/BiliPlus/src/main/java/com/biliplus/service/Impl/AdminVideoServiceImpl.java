package com.biliplus.service.Impl;

import com.biliplus.constant.Notify;
import com.biliplus.constant.VideoStatus;
import com.biliplus.exception.BusinessException;
import com.biliplus.mapper.AdminVideoMapper;
import com.biliplus.pojo.entity.Video;
import com.biliplus.result.PageResult;
import com.biliplus.service.AdminVideoService;
import com.biliplus.service.DynamicService;
import com.biliplus.service.NotificationService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
public class AdminVideoServiceImpl implements AdminVideoService {

    /** 视频状态：-1用户删除 0待审 1正常 2下架 3审核不通过 */
    public static final int STATUS_PENDING = VideoStatus.PENDING;
    public static final int STATUS_NORMAL = VideoStatus.NORMAL;
    public static final int STATUS_OFFLINE = VideoStatus.OFFLINE;
    public static final int STATUS_REJECTED = VideoStatus.REJECTED;

    @Autowired
    private AdminVideoMapper adminVideoMapper;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private DynamicService dynamicService;

    @Override
    public PageResult pageByStatus(Integer status, Integer page, Integer pageSize) {
        int p = page == null || page < 1 ? 1 : page;
        int ps = pageSize == null || pageSize < 1 ? 20 : Math.min(pageSize, 100);
        PageHelper.startPage(p, ps);
        Page<Video> result = adminVideoMapper.selectByStatus(status);
        return new PageResult(result.getTotal(), result.getResult());
    }

    @Override
    public Video getById(Long videoId) {
        if (videoId == null) {
            return null;
        }
        return adminVideoMapper.selectById(videoId);
    }

    @Override
    @Transactional
    public void approve(Long videoId) {
        changeStatus(videoId, STATUS_PENDING, STATUS_NORMAL, "审核通过失败：视频不存在或状态不正确");
        // 通过时清空历史拒绝原因
        adminVideoMapper.clearRejectReason(videoId);
        notifyAuthor(videoId, "你的稿件已通过审核",
                "《" + videoTitle(videoId) + "》已通过审核，现在可以在首页与频道页看到它了。");
        publishDynamic(videoId);
    }

    @Override
    @Transactional
    public void reject(Long videoId, String reason) {
        if (videoId == null) {
            throw new BusinessException("视频ID不能为空");
        }
        if (!StringUtils.hasText(reason)) {
            throw new BusinessException("请填写不通过原因");
        }
        String trimmed = reason.trim();
        if (trimmed.length() > 500) {
            throw new BusinessException("原因不能超过500字");
        }
        int rows = adminVideoMapper.reject(videoId, STATUS_PENDING, STATUS_REJECTED, trimmed);
        if (rows <= 0) {
            throw new BusinessException("审核不通过失败：视频不存在或状态不正确");
        }
        log.info("视频审核不通过 id={} reason={}", videoId, trimmed);
        notifyAuthor(videoId, "你的稿件未通过审核", "原因：" + trimmed);
    }

    @Override
    public void offline(Long videoId) {
        changeStatus(videoId, STATUS_NORMAL, STATUS_OFFLINE, "下架失败：视频不存在或状态不正确");
        notifyAuthor(videoId, "你的稿件已被下架",
                "《" + videoTitle(videoId) + "》已被下架，如有疑问请联系管理员。");
    }

    /** 审核结果通知作者本人 */
    private void notifyAuthor(Long videoId, String title, String content) {
        Video video = adminVideoMapper.selectById(videoId);
        if (video == null || video.getUserId() == null) {
            return;
        }
        notificationService.notify(video.getUserId(), null, Notify.TYPE_AUDIT,
                title, content, Notify.BIZ_VIDEO, videoId);
    }

    private String videoTitle(Long videoId) {
        Video video = adminVideoMapper.selectById(videoId);
        return video == null || video.getTitle() == null ? "你的稿件" : video.getTitle();
    }

    /** 审核通过即自动发一条投稿动态，作者主页与关注流都能看到 */
    private void publishDynamic(Long videoId) {
        try {
            dynamicService.publishVideoDynamic(videoId);
        } catch (Exception e) {
            log.warn("自动发布投稿动态失败 videoId={}", videoId, e);
        }
    }

    private void changeStatus(Long videoId, int fromStatus, int toStatus, String errMsg) {
        if (videoId == null) {
            throw new BusinessException("视频ID不能为空");
        }
        int rows = adminVideoMapper.updateStatus(videoId, fromStatus, toStatus);
        if (rows <= 0) {
            throw new BusinessException(errMsg);
        }
        log.info("视频状态变更 id={} {} -> {}", videoId, fromStatus, toStatus);
    }
}
