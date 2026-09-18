package com.biliplus.mapper;

import com.biliplus.pojo.entity.LiveRoom;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface LiveRoomMapper {

    @Insert("INSERT INTO live_room(title, cover_url, user_id, category_id, status, view_count, " +
            "start_time, stream_key, play_url, push_url, create_time, update_time) " +
            "VALUES(#{title}, #{coverUrl}, #{userId}, #{categoryId}, #{status}, #{viewCount}, " +
            "#{startTime}, #{streamKey}, #{playUrl}, #{pushUrl}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(LiveRoom room);

    @Select("SELECT * FROM live_room WHERE id = #{id}")
    LiveRoom selectById(@Param("id") Long id);

    @Select("SELECT * FROM live_room WHERE user_id = #{userId} AND status = 1 LIMIT 1")
    LiveRoom selectLiveByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM live_room WHERE status = 1 ORDER BY start_time DESC LIMIT #{offset}, #{size}")
    List<LiveRoom> listLive(@Param("offset") int offset, @Param("size") int size);

    @Select("SELECT COUNT(*) FROM live_room WHERE status = 1")
    long countLive();

    @Select("SELECT * FROM live_room WHERE 1=1 " +
            "AND (#{status} IS NULL OR status = #{status}) " +
            "ORDER BY id DESC LIMIT #{offset}, #{size}")
    List<LiveRoom> adminList(@Param("status") Integer status,
                             @Param("offset") int offset,
                             @Param("size") int size);

    @Select("<script>" +
            "SELECT COUNT(*) FROM live_room WHERE 1=1 " +
            "<if test='status != null'> AND status = #{status}</if>" +
            "</script>")
    long adminCount(@Param("status") Integer status);

    @Update("UPDATE live_room SET status = #{status}, end_time = #{endTime}, " +
            "stream_key = #{streamKey}, play_url = #{playUrl}, push_url = #{pushUrl}, update_time = NOW() " +
            "WHERE id = #{id}")
    int updateStatus(LiveRoom room);

    @Update("UPDATE live_room SET view_count = view_count + #{delta} WHERE id = #{id}")
    int incrViewCount(@Param("id") Long id, @Param("delta") int delta);
}
