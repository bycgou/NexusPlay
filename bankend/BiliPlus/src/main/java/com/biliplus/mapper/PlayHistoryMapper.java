package com.biliplus.mapper;

import com.biliplus.pojo.entity.PlayHistory;
import com.biliplus.pojo.vo.PlayHistoryVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PlayHistoryMapper {

    /** 同一视频只保留最新进度 */
    @Insert("INSERT INTO play_history(user_id, video_id, progress_sec, duration_sec, last_play_time, create_time) " +
            "VALUES(#{userId}, #{videoId}, #{progressSec}, #{durationSec}, NOW(), NOW()) " +
            "ON DUPLICATE KEY UPDATE progress_sec = VALUES(progress_sec), " +
            "duration_sec = VALUES(duration_sec), last_play_time = NOW()")
    void upsert(PlayHistory history);

    @Select("SELECT * FROM play_history WHERE user_id = #{userId} AND video_id = #{videoId}")
    PlayHistory selectOne(@Param("userId") Long userId, @Param("videoId") Long videoId);

    /** 已删除/下架的稿件不再出现在历史里 */
    @Select("SELECT h.id, h.video_id, h.progress_sec, h.duration_sec, h.last_play_time, " +
            "v.title, v.cover_url, v.user_id, u.nickname " +
            "FROM play_history h " +
            "JOIN video v ON v.id = h.video_id AND v.status = 1 " +
            "LEFT JOIN user u ON u.id = v.user_id " +
            "WHERE h.user_id = #{userId} " +
            "ORDER BY h.last_play_time DESC LIMIT #{offset}, #{size}")
    List<PlayHistoryVO> pageByUser(@Param("userId") Long userId,
                                  @Param("offset") int offset,
                                  @Param("size") int size);

    @Select("SELECT COUNT(*) FROM play_history h " +
            "JOIN video v ON v.id = h.video_id AND v.status = 1 " +
            "WHERE h.user_id = #{userId}")
    long countByUser(@Param("userId") Long userId);

    @Delete("DELETE FROM play_history WHERE user_id = #{userId} AND video_id = #{videoId}")
    int deleteOne(@Param("userId") Long userId, @Param("videoId") Long videoId);

    @Delete("DELETE FROM play_history WHERE user_id = #{userId}")
    int deleteAll(@Param("userId") Long userId);
}
