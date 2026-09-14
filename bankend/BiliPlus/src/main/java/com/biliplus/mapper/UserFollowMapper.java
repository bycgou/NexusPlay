package com.biliplus.mapper;

import com.biliplus.pojo.entity.UserFollow;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserFollowMapper {

    @Insert("INSERT INTO user_follow(user_id, follow_user_id, create_time) VALUES(#{userId}, #{followUserId}, #{createTime})")
    void insert(UserFollow userFollow);

    @Delete("DELETE FROM user_follow WHERE user_id = #{userId} AND follow_user_id = #{followUserId}")
    void delete(@Param("userId") Long userId, @Param("followUserId") Long followUserId);

    @Select("SELECT COUNT(1) FROM user_follow WHERE user_id = #{userId} AND follow_user_id = #{followUserId}")
    int countByPair(@Param("userId") Long userId, @Param("followUserId") Long followUserId);

    @Select("SELECT COUNT(1) FROM user_follow WHERE follow_user_id = #{userId}")
    long countFans(@Param("userId") Long userId);

    @Select("SELECT COUNT(1) FROM user_follow WHERE user_id = #{userId}")
    long countFollowing(@Param("userId") Long userId);
}
