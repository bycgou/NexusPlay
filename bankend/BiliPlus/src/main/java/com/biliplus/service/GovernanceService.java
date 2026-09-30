package com.biliplus.service;

import java.util.Map;

/** 治理报表聚合：为论文第 6 章的指标提供数据 */
public interface GovernanceService {

    /** 总览：举报处理时效、重复率、敏感词误伤率、处置复发率、信用分分布 */
    Map<String, Object> overview();

    /** 举报 SLA：待处理、超时、平均处理时长、处理人工作量 */
    Map<String, Object> reportSla();

    /** 近 N 日处理时效趋势 */
    Map<String, Object> timelinessTrend(int days);

    /** Top 违规用户（信用分最低） */
    Map<String, Object> topViolators(int limit);

    /**
     * 违规曝光率：窗口内 video_view 中，目标为违规视频的占比。
     * 违规视频 = 举报成立 ∪ 命中敏感词 ∪ 已下架/驳回。
     * 返回汇总与按天趋势，用于治理介入前后的对比曲线。
     */
    Map<String, Object> violationExposure(int days);
}
