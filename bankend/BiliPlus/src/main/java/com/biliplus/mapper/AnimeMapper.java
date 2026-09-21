package com.biliplus.mapper;

import com.biliplus.pojo.entity.Anime;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AnimeMapper {

    /**
     * 番剧分页列表
     * status: 0-未播出 1-连载中 2-已完结；null 查全部
     */
    @Select("<script>" +
            "SELECT * FROM anime " +
            "WHERE 1=1 " +
            "<if test='status != null'> AND status = #{status} </if>" +
            "<if test='keyword != null and keyword != \"\"'>" +
            " AND title LIKE CONCAT('%', #{keyword}, '%')" +
            "</if>" +
            " ORDER BY score DESC, follower_count DESC, id DESC" +
            "</script>")
    Page<Anime> pageQuery(@Param("status") Integer status, @Param("keyword") String keyword);

    @Select("SELECT * FROM anime WHERE id = #{id}")
    Anime selectById(@Param("id") Long id);
}
