package com.biliplus.mapper;

import com.biliplus.pojo.entity.Video;
import com.biliplus.pojo.entity.VideoLike;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.*;

/**
 * 视频点赞 Mapper，底层操作 user_like（targetType=1 表示视频）
 */
@Mapper
public interface VideoLikeMapper {

    /** 视频点赞目标类型 */
    Integer TARGET_TYPE_VIDEO = 1;

    @Insert("INSERT INTO user_like(user_id, target_id, target_type, create_time) VALUES(#{userId}, #{videoId}, 1, #{createTime})")
    void insert(VideoLike videoLike);

    @Delete("DELETE FROM user_like WHERE user_id = #{userId} AND target_id = #{videoId} AND target_type = 1")
    void delete(@Param("videoId") Long videoId, @Param("userId") Long userId);

    @Select("SELECT COUNT(1) FROM user_like WHERE user_id = #{userId} AND target_id = #{videoId} AND target_type = 1")
    int countByVideoAndUser(@Param("videoId") Long videoId, @Param("userId") Long userId);

    @Select("SELECT COUNT(1) FROM user_like WHERE target_id = #{videoId} AND target_type = 1")
    long countByVideoId(@Param("videoId") Long videoId);

    /** 用户点赞的已公开视频列表（按点赞时间倒序） */
    @Select("SELECT v.* FROM user_like ul " +
            "INNER JOIN video v ON ul.target_id = v.id " +
            "WHERE ul.user_id = #{userId} AND ul.target_type = 1 AND v.status = 1 " +
            "ORDER BY ul.create_time DESC")
    Page<Video> pageLikedVideos(@Param("userId") Long userId);
}
