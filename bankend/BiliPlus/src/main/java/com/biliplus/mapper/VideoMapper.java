package com.biliplus.mapper;

import com.biliplus.pojo.dto.userdto.VideoPageQueryDTO;
import com.biliplus.pojo.entity.Video;
import com.biliplus.pojo.vo.GetListVideoVO;
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

    /** 带作者昵称/头像的推荐列表（见 VideoMapper.xml） */
    Page<GetListVideoVO> recommend();

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

    /** 仅统计已通过（status=1）的稿件，避免给未审核视频刷分享数 */
    @Update("update video set share_count = share_count + 1 where id = #{videoId} and status = 1")
    int increaseShareCount(@Param("videoId") Long videoId);

    @Select("select share_count from video where id = #{videoId}")
    Integer selectShareCount(@Param("videoId") Long videoId);
}
