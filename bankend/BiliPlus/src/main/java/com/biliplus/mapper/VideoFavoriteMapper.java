package com.biliplus.mapper;

import com.biliplus.pojo.entity.VideoFavorite;
import org.apache.ibatis.annotations.*;

@Mapper
public interface VideoFavoriteMapper {

    @Insert("INSERT INTO video_favorite(video_id, user_id, create_time) VALUES(#{videoId}, #{userId}, #{createTime})")
    void insert(VideoFavorite videoFavorite);

    @Delete("DELETE FROM video_favorite WHERE video_id = #{videoId} AND user_id = #{userId}")
    void delete(@Param("videoId") Long videoId, @Param("userId") Long userId);

    @Select("SELECT COUNT(1) FROM video_favorite WHERE video_id = #{videoId} AND user_id = #{userId}")
    int countByVideoAndUser(@Param("videoId") Long videoId, @Param("userId") Long userId);

    @Select("SELECT COUNT(1) FROM video_favorite WHERE video_id = #{videoId}")
    long countByVideoId(@Param("videoId") Long videoId);
}
