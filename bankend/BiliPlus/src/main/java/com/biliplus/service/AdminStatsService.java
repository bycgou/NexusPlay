package com.biliplus.service;

import java.util.Map;

/** 平台数据看板：今日/累计 KPI 与近 N 日趋势 */
public interface AdminStatsService {

    /** 今日/累计：用户、投稿、待审、在线直播、礼物收入、弹幕、评论、待处理举报 */
    Map<String, Object> overview();

    /** 近 N 日注册 / 投稿 / 礼物曲线 */
    Map<String, Object> trend(int days);
}
