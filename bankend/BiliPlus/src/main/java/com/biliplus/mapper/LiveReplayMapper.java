package com.biliplus.mapper;

import com.biliplus.pojo.entity.LiveReplay;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface LiveReplayMapper {

    @Insert("INSERT INTO live_replay(live_room_id, user_id, title, cover_url, play_url, duration_sec, status, create_time) " +
            "VALUES(#{liveRoomId}, #{userId}, #{title}, #{coverUrl}, #{playUrl}, #{durationSec}, #{status}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(LiveReplay replay);

    @Select("SELECT * FROM live_replay WHERE id = #{id} AND status = 1")
    LiveReplay selectById(@Param("id") Long id);

    @Select("<script>" +
            "SELECT * FROM live_replay WHERE status = 1 " +
            "<if test='userId != null'> AND user_id = #{userId}</if> " +
            "ORDER BY id DESC LIMIT #{offset}, #{size}" +
            "</script>")
    List<LiveReplay> list(@Param("userId") Long userId,
                          @Param("offset") int offset,
                          @Param("size") int size);

    @Select("<script>" +
            "SELECT COUNT(*) FROM live_replay WHERE status = 1 " +
            "<if test='userId != null'> AND user_id = #{userId}</if>" +
            "</script>")
    long count(@Param("userId") Long userId);
}
