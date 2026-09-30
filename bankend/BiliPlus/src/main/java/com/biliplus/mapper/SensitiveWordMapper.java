package com.biliplus.mapper;

import com.biliplus.pojo.entity.SensitiveWord;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface SensitiveWordMapper {

    @Insert("INSERT INTO sensitive_word(word, level, status, create_time) VALUES(#{word}, #{level}, #{status}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(SensitiveWord word);

    @Select("SELECT * FROM sensitive_word WHERE id = #{id}")
    SensitiveWord selectById(@Param("id") Long id);

    @Select("SELECT * FROM sensitive_word WHERE word = #{word}")
    SensitiveWord selectByWord(@Param("word") String word);

    /** 启用中的全量词库，服务启动时加载并缓存 */
    @Select("SELECT * FROM sensitive_word WHERE status = 1")
    List<SensitiveWord> selectAllEnabled();

    @Select("<script>" +
            "SELECT * FROM sensitive_word WHERE 1=1 " +
            "<if test='keyword != null and keyword != \"\"'> AND word LIKE CONCAT('%', #{keyword}, '%')</if> " +
            "<if test='status != null'> AND status = #{status}</if> " +
            "ORDER BY id DESC LIMIT #{offset}, #{size}" +
            "</script>")
    List<SensitiveWord> adminList(@Param("keyword") String keyword,
                                  @Param("status") Integer status,
                                  @Param("offset") int offset,
                                  @Param("size") int size);

    @Select("<script>" +
            "SELECT COUNT(*) FROM sensitive_word WHERE 1=1 " +
            "<if test='keyword != null and keyword != \"\"'> AND word LIKE CONCAT('%', #{keyword}, '%')</if> " +
            "<if test='status != null'> AND status = #{status}</if>" +
            "</script>")
    long adminCount(@Param("keyword") String keyword, @Param("status") Integer status);

    @Update("UPDATE sensitive_word SET level = #{level}, status = #{status} WHERE id = #{id}")
    int update(@Param("id") Long id,
               @Param("level") Integer level,
               @Param("status") Integer status);

    @Delete("DELETE FROM sensitive_word WHERE id = #{id}")
    int delete(@Param("id") Long id);
}
