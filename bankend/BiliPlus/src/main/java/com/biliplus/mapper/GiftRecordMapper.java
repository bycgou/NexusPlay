package com.biliplus.mapper;

import com.biliplus.pojo.entity.GiftRecord;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface GiftRecordMapper {

    @Insert("INSERT INTO gift_record(live_room_id, pk_id, gift_id, sender_id, host_user_id, " +
            "unit_price, count, total_price, create_time) " +
            "VALUES(#{liveRoomId}, #{pkId}, #{giftId}, #{senderId}, #{hostUserId}, " +
            "#{unitPrice}, #{count}, #{totalPrice}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(GiftRecord record);

    @Select("SELECT * FROM gift_record WHERE live_room_id = #{roomId} ORDER BY id DESC LIMIT #{offset}, #{size}")
    List<GiftRecord> listByRoom(@Param("roomId") Long roomId,
                                @Param("offset") int offset,
                                @Param("size") int size);

    @Select("<script>" +
            "SELECT * FROM gift_record WHERE 1=1 " +
            "<if test='roomId != null'> AND live_room_id = #{roomId}</if> " +
            "<if test='senderId != null'> AND sender_id = #{senderId}</if> " +
            "<if test='hostUserId != null'> AND host_user_id = #{hostUserId}</if> " +
            "ORDER BY id DESC LIMIT #{offset}, #{size}" +
            "</script>")
    List<GiftRecord> adminList(@Param("roomId") Long roomId,
                               @Param("senderId") Long senderId,
                               @Param("hostUserId") Long hostUserId,
                               @Param("offset") int offset,
                               @Param("size") int size);

    @Select("<script>" +
            "SELECT COUNT(*) FROM gift_record WHERE 1=1 " +
            "<if test='roomId != null'> AND live_room_id = #{roomId}</if> " +
            "<if test='senderId != null'> AND sender_id = #{senderId}</if> " +
            "<if test='hostUserId != null'> AND host_user_id = #{hostUserId}</if>" +
            "</script>")
    long adminCount(@Param("roomId") Long roomId,
                    @Param("senderId") Long senderId,
                    @Param("hostUserId") Long hostUserId);
}
