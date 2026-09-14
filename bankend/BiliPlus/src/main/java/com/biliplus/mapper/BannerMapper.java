package com.biliplus.mapper;

import com.biliplus.pojo.entity.Banner;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface BannerMapper {

    @Select("SELECT * FROM banner ORDER BY sort_order ASC, id ASC")
    List<Banner> listAll();

    @Select("SELECT * FROM banner WHERE status = 1 ORDER BY sort_order ASC, id ASC")
    List<Banner> listOnline();

    @Select("SELECT * FROM banner WHERE id = #{id}")
    Banner selectById(@Param("id") Long id);

    @Insert("INSERT INTO banner(title, description, image_url, link_type, video_id, link_url, sort_order, status, create_time, update_time) " +
            "VALUES(#{title}, #{description}, #{imageUrl}, #{linkType}, #{videoId}, #{linkUrl}, #{sortOrder}, #{status}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Banner banner);

    @Update("UPDATE banner SET title = #{title}, description = #{description}, image_url = #{imageUrl}, " +
            "link_type = #{linkType}, video_id = #{videoId}, link_url = #{linkUrl}, " +
            "sort_order = #{sortOrder}, status = #{status}, update_time = #{updateTime} WHERE id = #{id}")
    int update(Banner banner);

    @Delete("DELETE FROM banner WHERE id = #{id}")
    int deleteById(@Param("id") Long id);
}
