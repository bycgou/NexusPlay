package com.biliplus.mapper;

import com.biliplus.pojo.entity.SensitiveHitLog;
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
public interface SensitiveHitLogMapper {

    @Insert("INSERT INTO sensitive_hit_log(word_id, word, level, user_id, target_type, target_id, " +
            "content, action, review_status, create_time) " +
            "VALUES(#{wordId}, #{word}, #{level}, #{userId}, #{targetType}, #{targetId}, " +
            "#{content}, #{action}, 0, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(SensitiveHitLog log);

    @Select("<script>" +
            "SELECT * FROM sensitive_hit_log WHERE 1=1 " +
            "<if test='reviewStatus != null'> AND review_status = #{reviewStatus}</if> " +
            "<if test='action != null and action != \"\"'> AND action = #{action}</if> " +
            "ORDER BY id DESC LIMIT #{offset}, #{size}" +
            "</script>")
    List<SensitiveHitLog> adminList(@Param("reviewStatus") Integer reviewStatus,
                                    @Param("action") String action,
                                    @Param("offset") int offset,
                                    @Param("size") int size);

    @Select("<script>" +
            "SELECT COUNT(*) FROM sensitive_hit_log WHERE 1=1 " +
            "<if test='reviewStatus != null'> AND review_status = #{reviewStatus}</if> " +
            "<if test='action != null and action != \"\"'> AND action = #{action}</if>" +
            "</script>")
    long adminCount(@Param("reviewStatus") Integer reviewStatus,
                    @Param("action") String action);

    /** 只有待复核记录可被复核，保证幂等 */
    @Update("UPDATE sensitive_hit_log SET review_status = #{reviewStatus}, " +
            "review_admin_id = #{adminId}, review_time = #{reviewTime} " +
            "WHERE id = #{id} AND review_status = 0")
    int review(@Param("id") Long id,
               @Param("reviewStatus") Integer reviewStatus,
               @Param("adminId") Long adminId,
               @Param("reviewTime") LocalDateTime reviewTime);

    /** 误伤率分母/分子、命中类型分布 */
    @Select("SELECT " +
            "COUNT(*) AS total, " +
            "SUM(CASE WHEN review_status = 1 THEN 1 ELSE 0 END) AS confirmed, " +
            "SUM(CASE WHEN review_status = 2 THEN 1 ELSE 0 END) AS falsePositive " +
            "FROM sensitive_hit_log")
    Map<String, Object> reviewSummary();

    /** 按目标类型统计命中分布 */
    @Select("SELECT target_type AS targetType, COUNT(*) AS cnt FROM sensitive_hit_log " +
            "GROUP BY target_type ORDER BY cnt DESC")
    List<Map<String, Object>> countByTargetType();

    /** Top 命中词，用于调整词库 */
    @Select("SELECT word, COUNT(*) AS cnt FROM sensitive_hit_log " +
            "GROUP BY word ORDER BY cnt DESC LIMIT #{limit}")
    List<Map<String, Object>> topWords(@Param("limit") int limit);
}
