package com.biliplus.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** 平台数据看板聚合查询 */
@Mapper
public interface AdminStatsMapper {

    /** 今日/累计 KPI。口径与各业务表 count 一致，允许近实时 */
    @Select("SELECT " +
            "(SELECT COUNT(*) FROM user) AS totalUsers, " +
            "(SELECT COUNT(*) FROM user WHERE create_time >= #{todayStart}) AS todayUsers, " +
            "(SELECT COUNT(*) FROM video WHERE status <> -1) AS totalVideos, " +
            "(SELECT COUNT(*) FROM video WHERE create_time >= #{todayStart}) AS todayVideos, " +
            "(SELECT COUNT(*) FROM video WHERE status = 0) AS pendingVideos, " +
            "(SELECT COUNT(*) FROM live_room WHERE status = 1) AS liveRooms, " +
            "(SELECT IFNULL(SUM(total_price), 0) FROM gift_record) AS totalGiftAmount, " +
            "(SELECT IFNULL(SUM(total_price), 0) FROM gift_record WHERE create_time >= #{todayStart}) AS todayGiftAmount, " +
            "(SELECT COUNT(*) FROM danmaku) AS totalDanmaku, " +
            "(SELECT COUNT(*) FROM danmaku WHERE create_time >= #{todayStart}) AS todayDanmaku, " +
            "(SELECT COUNT(*) FROM comment WHERE status = 1) AS totalComments, " +
            "(SELECT COUNT(*) FROM report WHERE status = 0) AS pendingReports, " +
            "(SELECT COUNT(*) FROM event_log) AS totalEvents")
    Map<String, Object> overview(@Param("todayStart") LocalDateTime todayStart);

    /** 近 N 日新增投稿 */
    @Select("SELECT DATE(create_time) AS day, COUNT(*) AS cnt FROM video " +
            "WHERE create_time >= #{from} GROUP BY DATE(create_time) ORDER BY day ASC")
    List<Map<String, Object>> dailyVideos(@Param("from") LocalDateTime from);

    /** 近 N 日新增注册 */
    @Select("SELECT DATE(create_time) AS day, COUNT(*) AS cnt FROM user " +
            "WHERE create_time >= #{from} GROUP BY DATE(create_time) ORDER BY day ASC")
    List<Map<String, Object>> dailyUsers(@Param("from") LocalDateTime from);

    /** 近 N 日礼物收入（金额与笔数） */
    @Select("SELECT DATE(create_time) AS day, IFNULL(SUM(total_price), 0) AS amount, COUNT(*) AS cnt " +
            "FROM gift_record WHERE create_time >= #{from} GROUP BY DATE(create_time) ORDER BY day ASC")
    List<Map<String, Object>> dailyGifts(@Param("from") LocalDateTime from);
}
