package com.biliplus.mapper;

import com.biliplus.pojo.entity.Category;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CategoryMapper {

    @Select("SELECT * FROM category ORDER BY sort_order ASC, id ASC")
    List<Category> listAll();

    @Select("SELECT * FROM category WHERE type = #{type} ORDER BY sort_order ASC, id ASC")
    List<Category> listByType(@Param("type") Integer type);

    @Select("SELECT * FROM category WHERE id = #{id}")
    Category selectById(@Param("id") Integer id);

    @Insert("INSERT INTO category(name, parent_id, sort_order, icon, type, create_time, update_time) " +
            "VALUES(#{name}, #{parentId}, #{sortOrder}, #{icon}, #{type}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Category category);

    @Update("UPDATE category SET name = #{name}, parent_id = #{parentId}, " +
            "sort_order = #{sortOrder}, icon = #{icon}, type = #{type}, update_time = #{updateTime} WHERE id = #{id}")
    int update(Category category);

    @Delete("DELETE FROM category WHERE id = #{id}")
    int deleteById(@Param("id") Integer id);

    @Select("SELECT COUNT(1) FROM video WHERE category_id = #{categoryId}")
    int countVideosByCategoryId(@Param("categoryId") Integer categoryId);

    @Select("SELECT COUNT(1) FROM live_room WHERE category_id = #{categoryId}")
    int countLiveRoomsByCategoryId(@Param("categoryId") Integer categoryId);
}
