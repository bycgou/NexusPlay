package com.biliplus.mapper;

import com.biliplus.pojo.entity.EventLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface EventLogMapper {

    @Insert("INSERT INTO event_log(user_id, event_type, target_type, target_id, " +
            "duration_sec, extra, source, create_time) " +
            "VALUES(#{userId}, #{eventType}, #{targetType}, #{targetId}, " +
            "#{durationSec}, #{extra}, #{source}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(EventLog eventLog);

    /**
     * 违规视频集合：举报成立的视频 + 命中敏感词的视频 + 已下架/驳回的视频。
     * 与 exposureSummary / exposureTrend 共用，改动时两边同步。
     */
    String VIOLATION_VIDEO_SET =
            "(SELECT r.target_id FROM report r WHERE r.target_type = 1 AND r.status = 1 " +
            " UNION " +
            " SELECT h.target_id FROM sensitive_hit_log h " +
            "        WHERE h.target_type = 'video' AND h.target_id IS NOT NULL " +
            " UNION " +
            " SELECT v.id FROM video v WHERE v.status IN (2, 3))";

    /** 违规曝光率汇总：窗口内 video_view 中目标为违规视频的占比 */
    @Select("SELECT COUNT(*) AS totalViews, " +
            "SUM(CASE WHEN target_id IN " + VIOLATION_VIDEO_SET + " THEN 1 ELSE 0 END) AS violationViews, " +
            "COUNT(DISTINCT target_id) AS distinctVideos " +
            "FROM event_log " +
            "WHERE event_type = 'video_view' AND create_time >= #{from}")
    Map<String, Object> exposureSummary(@Param("from") LocalDateTime from);

    /** 违规曝光率按天趋势，用于治理介入前后的对比曲线 */
    @Select("SELECT DATE(create_time) AS day, COUNT(*) AS totalViews, " +
            "SUM(CASE WHEN target_id IN " + VIOLATION_VIDEO_SET + " THEN 1 ELSE 0 END) AS violationViews " +
            "FROM event_log " +
            "WHERE event_type = 'video_view' AND create_time >= #{from} " +
            "GROUP BY DATE(create_time) ORDER BY day ASC")
    List<Map<String, Object>> exposureTrend(@Param("from") LocalDateTime from);

    /** 违规视频数量（集合规模，用于报表分母解释）。MySQL 要求派生表必须有别名 */
    @Select("SELECT COUNT(*) FROM " + VIOLATION_VIDEO_SET + " vio")
    long countViolationVideos();

    /** 窗口内各事件类型计数，看板与推荐特征的原料 */
    @Select("SELECT event_type AS eventType, COUNT(*) AS cnt FROM event_log " +
            "WHERE create_time >= #{from} GROUP BY event_type ORDER BY cnt DESC")
    List<Map<String, Object>> countByType(@Param("from") LocalDateTime from);
}
