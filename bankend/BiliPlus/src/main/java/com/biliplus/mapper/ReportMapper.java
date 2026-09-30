package com.biliplus.mapper;

import com.biliplus.pojo.entity.Report;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface ReportMapper {

    @Insert("INSERT INTO report(reporter_id, target_type, target_id, reason, detail, status, create_time) " +
            "VALUES(#{reporterId}, #{targetType}, #{targetId}, #{reason}, #{detail}, 0, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Report report);

    @Select("SELECT * FROM report WHERE id = #{id}")
    Report selectById(@Param("id") Long id);

    /** 同一用户对同一目标的未结案举报数，用于去重 */
    @Select("SELECT COUNT(*) FROM report WHERE reporter_id = #{reporterId} " +
            "AND target_type = #{targetType} AND target_id = #{targetId} AND status = 0")
    int countPending(@Param("reporterId") Long reporterId,
                     @Param("targetType") Integer targetType,
                     @Param("targetId") Long targetId);

    @Select("<script>" +
            "SELECT * FROM report WHERE 1=1 " +
            "<if test='status != null'> AND status = #{status}</if> " +
            "<if test='targetType != null'> AND target_type = #{targetType}</if> " +
            "ORDER BY id DESC LIMIT #{offset}, #{size}" +
            "</script>")
    List<Report> adminList(@Param("status") Integer status,
                           @Param("targetType") Integer targetType,
                           @Param("offset") int offset,
                           @Param("size") int size);

    @Select("<script>" +
            "SELECT COUNT(*) FROM report WHERE 1=1 " +
            "<if test='status != null'> AND status = #{status}</if> " +
            "<if test='targetType != null'> AND target_type = #{targetType}</if>" +
            "</script>")
    long adminCount(@Param("status") Integer status, @Param("targetType") Integer targetType);

    /** 只有待处理的举报能被处理，保证重复提交不会覆盖处理结果 */
    @Update("UPDATE report SET status = #{status}, handler_id = #{handlerId}, " +
            "handle_remark = #{remark}, handle_time = #{handleTime} " +
            "WHERE id = #{id} AND status = 0")
    int handle(@Param("id") Long id,
               @Param("status") Integer status,
               @Param("handlerId") Long handlerId,
               @Param("remark") String remark,
               @Param("handleTime") LocalDateTime handleTime);

    // ===== 治理报表：举报 SLA =====

    /** 总量/待处理/超时（>24h 未处理）/平均处理时长（分钟） */
    @Select("SELECT COUNT(*) AS total, " +
            "SUM(CASE WHEN status = 0 THEN 1 ELSE 0 END) AS pending, " +
            "SUM(CASE WHEN status = 0 AND TIMESTAMPDIFF(HOUR, create_time, NOW()) > 24 THEN 1 ELSE 0 END) AS overdue, " +
            "AVG(TIMESTAMPDIFF(MINUTE, create_time, handle_time)) AS avgHandleMinutes, " +
            "SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) AS upheld, " +
            "SUM(CASE WHEN status = 2 THEN 1 ELSE 0 END) AS rejected " +
            "FROM report")
    Map<String, Object> slaSummary();

    /** 处理人工作量 */
    @Select("SELECT handler_id AS handlerId, COUNT(*) AS cnt, " +
            "SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) AS upheld " +
            "FROM report WHERE handler_id IS NOT NULL AND status IN (1, 2) " +
            "GROUP BY handler_id ORDER BY cnt DESC")
    List<Map<String, Object>> handlerWorkload();

    /** 近 N 日处理时效趋势（按天） */
    @Select("SELECT DATE(create_time) AS day, COUNT(*) AS cnt, " +
            "AVG(TIMESTAMPDIFF(MINUTE, create_time, handle_time)) AS avgHandleMinutes " +
            "FROM report WHERE handle_time IS NOT NULL AND create_time >= #{from} " +
            "GROUP BY DATE(create_time) ORDER BY day ASC")
    List<Map<String, Object>> dailyTimeliness(@Param("from") LocalDateTime from);

    /** 举报重复率分母/分子：被举报目标数 vs 被多次举报的目标数 */
    @Select("SELECT COUNT(*) AS dupTargets FROM (" +
            "  SELECT target_type, target_id FROM report " +
            "  GROUP BY target_type, target_id HAVING COUNT(*) > 1) t")
    Map<String, Object> duplicateSummary();

    /** 举报类型分布 */
    @Select("SELECT target_type AS targetType, reason, COUNT(*) AS cnt FROM report " +
            "GROUP BY target_type, reason ORDER BY cnt DESC")
    List<Map<String, Object>> reasonDistribution();
}
