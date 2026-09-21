package com.biliplus.mapper;

import com.biliplus.pojo.entity.FavoriteFolder;
import com.biliplus.pojo.vo.FavoriteFolderVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface FavoriteFolderMapper {

    @Insert("INSERT INTO favorite_folder(user_id, name, is_default, is_private, create_time) " +
            "VALUES(#{userId}, #{name}, #{isDefault}, #{isPrivate}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(FavoriteFolder folder);

    @Select("SELECT * FROM favorite_folder WHERE id = #{id}")
    FavoriteFolder selectById(@Param("id") Long id);

    @Select("SELECT * FROM favorite_folder WHERE user_id = #{userId} AND is_default = 1 LIMIT 1")
    FavoriteFolder selectDefault(@Param("userId") Long userId);

    @Select("SELECT f.id, f.name, f.is_default, f.is_private, f.create_time, " +
            "(SELECT COUNT(*) FROM video_favorite vf WHERE vf.folder_id = f.id) AS video_count " +
            "FROM favorite_folder f WHERE f.user_id = #{userId} ORDER BY f.is_default DESC, f.id ASC")
    List<FavoriteFolderVO> listByUser(@Param("userId") Long userId);

    @Update("UPDATE favorite_folder SET name = #{name}, is_private = #{isPrivate} WHERE id = #{id} AND user_id = #{userId}")
    int update(@Param("id") Long id,
               @Param("userId") Long userId,
               @Param("name") String name,
               @Param("isPrivate") Integer isPrivate);

    @Delete("DELETE FROM favorite_folder WHERE id = #{id} AND user_id = #{userId} AND is_default = 0")
    int delete(@Param("id") Long id, @Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM favorite_folder WHERE user_id = #{userId} AND name = #{name}")
    int countByUserAndName(@Param("userId") Long userId, @Param("name") String name);
}
