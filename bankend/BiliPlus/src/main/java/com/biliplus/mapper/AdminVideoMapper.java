package com.biliplus.mapper;

import com.biliplus.pojo.entity.Video;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AdminVideoMapper {

    @Select("<script>" +
            "SELECT * FROM video " +
            "<where>" +
            "<if test='status != null'> status = #{status} </if>" +
            "</where>" +
            "ORDER BY create_time DESC" +
            "</script>")
    Page<Video> selectByStatus(@Param("status") Integer status);

    @Select("SELECT * FROM video WHERE id = #{id}")
    Video selectById(@Param("id") Long id);

    @Update("UPDATE video SET status = #{toStatus}, update_time = NOW() WHERE id = #{videoId} AND status = #{fromStatus}")
    int updateStatus(@Param("videoId") Long videoId,
                     @Param("fromStatus") int fromStatus,
                     @Param("toStatus") int toStatus);

    @Update("UPDATE video SET status = #{toStatus}, reject_reason = #{reason}, update_time = NOW() " +
            "WHERE id = #{videoId} AND status = #{fromStatus}")
    int reject(@Param("videoId") Long videoId,
               @Param("fromStatus") int fromStatus,
               @Param("toStatus") int toStatus,
               @Param("reason") String reason);

    @Update("UPDATE video SET reject_reason = NULL WHERE id = #{videoId}")
    int clearRejectReason(@Param("videoId") Long videoId);
}
