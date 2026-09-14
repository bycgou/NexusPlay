package com.biliplus.mapper;

import com.biliplus.pojo.entity.UserLike;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserLikeMapper {

    @Insert("INSERT INTO user_like(user_id, target_id, target_type, create_time) VALUES(#{userId}, #{targetId}, #{targetType}, #{createTime})")
    void insert(UserLike userLike);

    @Delete("DELETE FROM user_like WHERE user_id = #{userId} AND target_id = #{targetId} AND target_type = #{targetType}")
    void delete(@Param("userId") Long userId, @Param("targetId") Long targetId, @Param("targetType") Integer targetType);

    @Select("SELECT COUNT(1) FROM user_like WHERE user_id = #{userId} AND target_id = #{targetId} AND target_type = #{targetType}")
    int countByUserAndTarget(@Param("userId") Long userId, @Param("targetId") Long targetId, @Param("targetType") Integer targetType);

    @Select("SELECT COUNT(1) FROM user_like WHERE target_id = #{targetId} AND target_type = #{targetType}")
    long countByTargetId(@Param("targetId") Long targetId, @Param("targetType") Integer targetType);
}
