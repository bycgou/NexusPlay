package com.biliplus.mapper;

import com.biliplus.pojo.entity.UserCredit;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserCreditMapper {

    @Insert("INSERT INTO user_credit(user_id, score, violation_count, last_violation_time, update_time) " +
            "VALUES(#{userId}, 100, 0, NULL, NOW()) ON DUPLICATE KEY UPDATE user_id = user_id")
    void ensure(@Param("userId") Long userId);

    @Select("SELECT * FROM user_credit WHERE user_id = #{userId}")
    UserCredit selectByUserId(@Param("userId") Long userId);

    /** 扣分：violation 计数与最近违规时间仅在扣分时累加 */
    @Update("UPDATE user_credit SET score = GREATEST(score + #{delta}, 0), " +
            "violation_count = violation_count + #{violation}, " +
            "last_violation_time = IF(#{violation} > 0, NOW(), last_violation_time) " +
            "WHERE user_id = #{userId}")
    int changeScore(@Param("userId") Long userId,
                    @Param("delta") int delta,
                    @Param("violation") int violation);

    /** 信用分最低的用户，治理报表的 Top 违规榜 */
    @Select("SELECT c.user_id AS userId, c.score, c.violation_count AS violationCount, " +
            "c.last_violation_time AS lastViolationTime, u.nickname, u.username " +
            "FROM user_credit c LEFT JOIN user u ON u.id = c.user_id " +
            "ORDER BY c.score ASC, c.violation_count DESC LIMIT #{limit}")
    List<Map<String, Object>> worstUsers(@Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM user_credit WHERE score < #{threshold}")
    long countBelow(@Param("threshold") int threshold);
}
