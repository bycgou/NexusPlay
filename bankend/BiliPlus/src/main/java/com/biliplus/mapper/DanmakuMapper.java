package com.biliplus.mapper;

import com.biliplus.pojo.entity.Danmaku;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DanmakuMapper {

    @Insert("insert into danmaku(video_id, user_id, content, time, color, type, status, create_time) " +
            "values(#{videoId}, #{userId}, #{content}, #{time}, #{color}, #{type}, #{status}, #{createTime})")
    void save(Danmaku danmaku);

    @Select("select * from danmaku where video_id = #{videoId} and status = 1 order by time asc limit #{maxCount}")
    List<Danmaku> getDanmakuById(@Param("videoId") Long videoId, @Param("maxCount") int maxCount);
}
