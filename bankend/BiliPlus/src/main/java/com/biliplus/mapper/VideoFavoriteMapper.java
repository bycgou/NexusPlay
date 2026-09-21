package com.biliplus.mapper;

import com.biliplus.pojo.entity.Video;
import com.biliplus.pojo.entity.VideoFavorite;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.*;

@Mapper
public interface VideoFavoriteMapper {

    @Insert("INSERT INTO video_favorite(video_id, folder_id, user_id, create_time) " +
            "VALUES(#{videoId}, #{folderId}, #{userId}, #{createTime}) " +
            "ON DUPLICATE KEY UPDATE folder_id = VALUES(folder_id), create_time = VALUES(create_time)")
    void insert(VideoFavorite videoFavorite);

    @Delete("DELETE FROM video_favorite WHERE video_id = #{videoId} AND user_id = #{userId}")
    void delete(@Param("videoId") Long videoId, @Param("userId") Long userId);

    @Select("SELECT COUNT(1) FROM video_favorite WHERE video_id = #{videoId} AND user_id = #{userId}")
    int countByVideoAndUser(@Param("videoId") Long videoId, @Param("userId") Long userId);

    @Select("SELECT COUNT(1) FROM video_favorite WHERE video_id = #{videoId}")
    long countByVideoId(@Param("videoId") Long videoId);

    /** 用户收藏的已公开视频列表（按收藏时间倒序） */
    @Select("SELECT v.* FROM video_favorite f " +
            "INNER JOIN video v ON f.video_id = v.id " +
            "WHERE f.user_id = #{userId} AND v.status = 1 " +
            "ORDER BY f.create_time DESC")
    Page<Video> pageFavoriteVideos(@Param("userId") Long userId);

    /** 指定收藏夹内的视频（folder_id 为 NULL 的历史数据按默认夹处理） */
    @Select("SELECT v.* FROM video_favorite f " +
            "INNER JOIN video v ON f.video_id = v.id " +
            "WHERE f.user_id = #{userId} AND v.status = 1 " +
            "AND (f.folder_id = #{folderId} OR (f.folder_id IS NULL AND #{isDefault} = 1)) " +
            "ORDER BY f.create_time DESC")
    Page<Video> pageFolderVideos(@Param("userId") Long userId,
                                 @Param("folderId") Long folderId,
                                 @Param("isDefault") int isDefault);

    @Select("SELECT * FROM video_favorite WHERE video_id = #{videoId} AND user_id = #{userId}")
    VideoFavorite selectOne(@Param("videoId") Long videoId, @Param("userId") Long userId);
}
