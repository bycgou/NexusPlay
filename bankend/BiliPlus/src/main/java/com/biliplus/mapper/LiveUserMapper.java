package com.biliplus.mapper;

import com.biliplus.pojo.entity.LiveUser;
import org.apache.ibatis.annotations.*;

@Mapper
public interface LiveUserMapper {

    @Insert("INSERT INTO live_user(live_room_id, user_id, enter_time) " +
            "VALUES(#{liveRoomId}, #{userId}, #{enterTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(LiveUser liveUser);

    @Update("UPDATE live_user SET leave_time = NOW() " +
            "WHERE live_room_id = #{roomId} AND user_id = #{userId} AND leave_time IS NULL")
    int markLeave(@Param("roomId") Long roomId, @Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM live_user WHERE live_room_id = #{roomId} AND leave_time IS NULL")
    long countOnline(@Param("roomId") Long roomId);
}
