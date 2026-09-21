package com.biliplus.service;

import com.biliplus.pojo.entity.Report;
import com.biliplus.result.PageResult;

public interface ReportService {

    /** 用户提交举报；同一目标未结案时不重复受理 */
    Report submit(Long reporterId, Integer targetType, Long targetId, Integer reason, String detail);

    PageResult adminList(Integer status, Integer targetType, Integer page, Integer size);

    /**
     * 管理端处理举报。
     * 举报成立时联动处置目标内容（视频下架 / 评论软删），驳回则只记录结论。
     */
    void handle(Long adminId, Long reportId, Integer status, String remark);
}
