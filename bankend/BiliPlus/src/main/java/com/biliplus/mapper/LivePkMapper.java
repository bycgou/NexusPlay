package com.biliplus.mapper;

import com.biliplus.pojo.entity.LivePk;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface LivePkMapper {

    @Insert("INSERT INTO live_pk(room_a_id, room_b_id, host_a_id, host_b_id, status, score_a, score_b, " +
            "start_time, end_time, duration_sec, create_time) " +
            "VALUES(#{roomAId}, #{roomBId}, #{hostAId}, #{hostBId}, #{status}, #{scoreA}, #{scoreB}, " +
            "#{startTime}, #{endTime}, #{durationSec}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(LivePk pk);

    @Select("SELECT * FROM live_pk WHERE id = #{id}")
    LivePk selectById(@Param("id") Long id);

    @Select("SELECT * FROM live_pk WHERE status = 1 AND (room_a_id = #{roomId} OR room_b_id = #{roomId}) LIMIT 1")
    LivePk selectActiveByRoom(@Param("roomId") Long roomId);

    @Select("SELECT * FROM live_pk WHERE status = 0 AND room_b_id = #{roomId} ORDER BY id DESC LIMIT 1")
    LivePk selectPendingInvite(@Param("roomId") Long roomId);

    /** 进行中的全部 PK，供定时任务判断是否到期 */
    @Select("SELECT * FROM live_pk WHERE status = 1")
    List<LivePk> selectAllActive();

    @Update("UPDATE live_pk SET status = #{status}, score_a = #{scoreA}, score_b = #{scoreB}, " +
            "start_time = #{startTime}, end_time = #{endTime} WHERE id = #{id}")
    int update(LivePk pk);

    @Update("UPDATE live_pk SET status = 2, end_time = NOW() WHERE status IN (0,1) AND (room_a_id = #{roomId} OR room_b_id = #{roomId})")
    int closeByRoom(@Param("roomId") Long roomId);

    @Update("UPDATE live_pk SET score_a = score_a + #{score} WHERE id = #{pkId} AND room_a_id = #{roomId}")
    int addScoreA(@Param("pkId") Long pkId, @Param("roomId") Long roomId, @Param("score") int score);

    @Update("UPDATE live_pk SET score_b = score_b + #{score} WHERE id = #{pkId} AND room_b_id = #{roomId}")
    int addScoreB(@Param("pkId") Long pkId, @Param("roomId") Long roomId, @Param("score") int score);
}
