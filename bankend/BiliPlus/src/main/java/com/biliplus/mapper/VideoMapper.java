package com.biliplus.mapper;

import com.biliplus.pojo.dto.userdto.VideoPageQueryDTO;
import com.biliplus.pojo.entity.Video;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface VideoMapper {
    Page<Video> pageQuery(VideoPageQueryDTO videoPageQueryDTO);

    @Select("select * from video where id = #{videoId} and status = 1")
    Video getVideo(Long videoId);

    @Select("select * from video where status = 1 order by rand()")
    Page<Video> recommend();

    @Update("update video set like_count = like_count + #{delta} where id = #{videoId}")
    int changeLikeCount(@Param("videoId") Long videoId, @Param("delta") int delta);

    @Update("update video set comment_count = comment_count + #{delta} where id = #{videoId}")
    int changeCommentCount(@Param("videoId") Long videoId, @Param("delta") int delta);

    default int increaseCommentCount(Long videoId) {
        return changeCommentCount(videoId, 1);
    }

    default int decreaseCommentCount(Long videoId) {
        return changeCommentCount(videoId, -1);
    }

    @Update("update video set view_count = view_count + 1 where id = #{videoId}")
    int increaseViewCount(@Param("videoId") Long videoId);
}
