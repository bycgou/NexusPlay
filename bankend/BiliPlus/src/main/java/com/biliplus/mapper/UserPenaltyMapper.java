package com.biliplus.mapper;

import com.biliplus.pojo.entity.UserPenalty;
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
public interface UserPenaltyMapper {

    @Insert("INSERT INTO user_penalty(user_id, action, reason, start_time, end_time, admin_id, status, create_time) " +
            "VALUES(#{userId}, #{action}, #{reason}, #{startTime}, #{endTime}, #{adminId}, #{status}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(UserPenalty penalty);

    @Select("SELECT * FROM user_penalty WHERE id = #{id}")
    UserPenalty selectById(@Param("id") Long id);

    /** 该用户当前是否有生效中的同类处置 */
    @Select("SELECT COUNT(*) FROM user_penalty WHERE user_id = #{userId} " +
            "AND action = #{action} AND status = 1")
    int countActive(@Param("userId") Long userId, @Param("action") String action);

    @Select("<script>" +
            "SELECT * FROM user_penalty WHERE 1=1 " +
            "<if test='userId != null'> AND user_id = #{userId}</if> " +
            "<if test='status != null'> AND status = #{status}</if> " +
            "ORDER BY id DESC LIMIT #{offset}, #{size}" +
            "</script>")
    List<UserPenalty> adminList(@Param("userId") Long userId,
                                @Param("status") Integer status,
                                @Param("offset") int offset,
                                @Param("size") int size);

    @Select("<script>" +
            "SELECT COUNT(*) FROM user_penalty WHERE 1=1 " +
            "<if test='userId != null'> AND user_id = #{userId}</if> " +
            "<if test='status != null'> AND status = #{status}</if>" +
            "</script>")
    long adminCount(@Param("userId") Long userId, @Param("status") Integer status);

    /** 到期未结束的处置，供定时任务批量解封 */
    @Select("SELECT * FROM user_penalty WHERE status = 1 AND end_time IS NOT NULL AND end_time <= #{now}")
    List<UserPenalty> selectExpired(@Param("now") LocalDateTime now);

    @Update("UPDATE user_penalty SET status = #{toStatus} WHERE id = #{id} AND status = 1")
    int changeStatus(@Param("id") Long id, @Param("toStatus") int toStatus);

    /** 提前解除：只结束该用户生效中的同类处置 */
    @Update("UPDATE user_penalty SET status = 3 WHERE user_id = #{userId} AND action = #{action} AND status = 1")
    int releaseActive(@Param("userId") Long userId, @Param("action") String action);

    /** 复发率：被处置过的用户中，之后又产生新处置的人数 */
    @Select("SELECT " +
            "COUNT(DISTINCT p1.user_id) AS penalizedUsers, " +
            "COUNT(DISTINCT CASE WHEN p2.id IS NOT NULL THEN p1.user_id END) AS recidivistUsers " +
            "FROM user_penalty p1 " +
            "LEFT JOIN user_penalty p2 " +
            "  ON p2.user_id = p1.user_id AND p2.id > p1.id AND p2.status = 1")
    Map<String, Object> recidivismSummary();
}
