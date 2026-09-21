package com.biliplus.mapper;

import com.biliplus.pojo.entity.LiveMicSession;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface LiveMicSessionMapper {

    @Insert("INSERT INTO live_mic_session(live_room_id, host_user_id, guest_user_id, status, create_time) " +
            "VALUES(#{liveRoomId}, #{hostUserId}, #{guestUserId}, #{status}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(LiveMicSession session);

    @Select("SELECT * FROM live_mic_session WHERE id = #{id}")
    LiveMicSession selectById(@Param("id") Long id);

    @Select("SELECT * FROM live_mic_session WHERE live_room_id = #{roomId} AND status = 0 " +
            "ORDER BY id DESC LIMIT #{limit}")
    List<LiveMicSession> listPending(@Param("roomId") Long roomId, @Param("limit") int limit);

    @Select("SELECT * FROM live_mic_session WHERE live_room_id = #{roomId} AND status = 1 LIMIT 1")
    LiveMicSession selectActive(@Param("roomId") Long roomId);

    @Update("UPDATE live_mic_session SET status = #{status}, start_time = #{startTime}, end_time = #{endTime} " +
            "WHERE id = #{id}")
    int updateStatus(LiveMicSession session);

    @Update("UPDATE live_mic_session SET status = 2, end_time = NOW() WHERE live_room_id = #{roomId} AND status IN (0,1)")
    int closeByRoom(@Param("roomId") Long roomId);
}
