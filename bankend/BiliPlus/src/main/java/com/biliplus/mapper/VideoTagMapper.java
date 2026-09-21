package com.biliplus.mapper;

import com.biliplus.pojo.entity.Tag;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface VideoTagMapper {

    @Select("select id, name, create_time from tag where name = #{name}")
    Tag selectByName(@Param("name") String name);

    /**
     * 标签名唯一，已存在时通过 LAST_INSERT_ID(id) 让 useGeneratedKeys 回填既有 ID，
     * 省一次 select 且并发下不会插入重复标签。
     */
    @Insert("insert into tag(name, create_time) values(#{name}, now()) " +
            "on duplicate key update id = last_insert_id(id)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void upsertTag(Tag tag);

    @Select("select tag_id from video_tag where video_id = #{videoId}")
    List<Long> selectTagIds(@Param("videoId") Long videoId);

    @Select("select t.id, t.name, t.create_time from tag t " +
            "join video_tag vt on vt.tag_id = t.id where vt.video_id = #{videoId} order by t.id")
    List<Tag> selectTagsByVideoId(@Param("videoId") Long videoId);

    @Delete("delete from video_tag where video_id = #{videoId}")
    int deleteByVideoId(@Param("videoId") Long videoId);

    @Insert("insert into video_tag(video_id, tag_id) values(#{videoId}, #{tagId}) " +
            "on duplicate key update video_id = video_id")
    int insertVideoTag(@Param("videoId") Long videoId, @Param("tagId") Long tagId);
}
