package com.biliplus.mapper;

import com.biliplus.pojo.entity.Comment;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CommentMapper {

    void save(Comment comment);

    Page<Comment> pageQuery(Long videoId);

    Comment selectById(@Param("id") Long id);

    int softDelete(@Param("id") Long id, @Param("userId") Long userId);

    @org.apache.ibatis.annotations.Update("UPDATE comment SET like_count = GREATEST(like_count + #{delta}, 0) WHERE id = #{id}")
    int changeLikeCount(@Param("id") Long id, @Param("delta") int delta);
}
