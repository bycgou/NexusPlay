package com.biliplus.mapper;

import com.biliplus.pojo.dto.userdto.ChatConversationDTO;
import com.biliplus.pojo.entity.User;
import com.biliplus.pojo.entity.Video;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.ArrayList;
import java.util.List;

@Mapper
public interface PeopleUserMapper {

    int countByEmail(String email);


    void insert(User user);

    @Select("select * from user where email=#{email}")
    User userlogin(String email);


    void update(User user);

    void insertVideo(Video video);

    @Select("select * from user where id=#{userId}")
    User getUserById(Long userId);

    // 根据id批量查询用户
    List<User> selectByIds(@Param("ids") List<Long> ids);

    @Select("select * from user where nickname=#{name}")
    User getUserByName(String name);

    /** 昵称/用户名模糊搜索（公开，只返回安全字段） */
    @Select("SELECT id, username, nickname, avatar, signature, create_time " +
            "FROM user " +
            "WHERE status = 1 AND (nickname LIKE CONCAT('%', #{keyword}, '%') " +
            "   OR username LIKE CONCAT('%', #{keyword}, '%')) " +
            "ORDER BY id DESC")
    List<User> searchByKeyword(@Param("keyword") String keyword);

    @Select("select * from video where user_id = #{userId} order by create_time desc")
    List<Video> selectVideosByUserId(@Param("userId") Long userId);

    /** 0-禁用 1-正常（直播违规封禁/解封） */
    @Update("UPDATE user SET status = #{status} WHERE id = #{userId}")
    int updateUserStatus(@Param("userId") Long userId, @Param("status") Integer status);

    @Update("UPDATE user SET password = #{password}, update_time = NOW() WHERE id = #{userId}")
    int updatePassword(@Param("userId") Long userId, @Param("password") String password);
}
