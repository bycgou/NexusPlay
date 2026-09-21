package com.biliplus.mapper;

import com.biliplus.pojo.entity.Report;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ReportMapper {

    @Insert("INSERT INTO report(reporter_id, target_type, target_id, reason, detail, status, create_time) " +
            "VALUES(#{reporterId}, #{targetType}, #{targetId}, #{reason}, #{detail}, 0, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Report report);

    @Select("SELECT * FROM report WHERE id = #{id}")
    Report selectById(@Param("id") Long id);

    /** 同一用户对同一目标的未结案举报数，用于去重 */
    @Select("SELECT COUNT(*) FROM report WHERE reporter_id = #{reporterId} " +
            "AND target_type = #{targetType} AND target_id = #{targetId} AND status = 0")
    int countPending(@Param("reporterId") Long reporterId,
                     @Param("targetType") Integer targetType,
                     @Param("targetId") Long targetId);

    @Select("<script>" +
            "SELECT * FROM report WHERE 1=1 " +
            "<if test='status != null'> AND status = #{status}</if> " +
            "<if test='targetType != null'> AND target_type = #{targetType}</if> " +
            "ORDER BY id DESC LIMIT #{offset}, #{size}" +
            "</script>")
    List<Report> adminList(@Param("status") Integer status,
                           @Param("targetType") Integer targetType,
                           @Param("offset") int offset,
                           @Param("size") int size);

    @Select("<script>" +
            "SELECT COUNT(*) FROM report WHERE 1=1 " +
            "<if test='status != null'> AND status = #{status}</if> " +
            "<if test='targetType != null'> AND target_type = #{targetType}</if>" +
            "</script>")
    long adminCount(@Param("status") Integer status, @Param("targetType") Integer targetType);

    /** 只有待处理的举报能被处理，保证重复提交不会覆盖处理结果 */
    @Update("UPDATE report SET status = #{status}, handler_id = #{handlerId}, " +
            "handle_remark = #{remark}, handle_time = #{handleTime} " +
            "WHERE id = #{id} AND status = 0")
    int handle(@Param("id") Long id,
               @Param("status") Integer status,
               @Param("handlerId") Long handlerId,
               @Param("remark") String remark,
               @Param("handleTime") LocalDateTime handleTime);
}
