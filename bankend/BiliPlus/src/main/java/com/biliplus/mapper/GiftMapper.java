package com.biliplus.mapper;

import com.biliplus.pojo.entity.Gift;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface GiftMapper {

    @Select("SELECT * FROM gift WHERE status = 1 ORDER BY sort_order ASC, id ASC")
    List<Gift> listOnline();

    @Select("SELECT * FROM gift ORDER BY sort_order ASC, id ASC")
    List<Gift> listAll();

    @Select("SELECT * FROM gift WHERE id = #{id}")
    Gift selectById(@Param("id") Long id);

    @Insert("INSERT INTO gift(name, icon_url, price, effect_level, sort_order, status, create_time) " +
            "VALUES(#{name}, #{iconUrl}, #{price}, #{effectLevel}, #{sortOrder}, #{status}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Gift gift);

    @Update("UPDATE gift SET name = #{name}, icon_url = #{iconUrl}, price = #{price}, " +
            "effect_level = #{effectLevel}, sort_order = #{sortOrder}, status = #{status} WHERE id = #{id}")
    int update(Gift gift);

    @Delete("DELETE FROM gift WHERE id = #{id}")
    int deleteById(@Param("id") Long id);
}
