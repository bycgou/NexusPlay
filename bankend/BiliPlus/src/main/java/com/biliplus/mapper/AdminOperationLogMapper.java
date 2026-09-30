package com.biliplus.mapper;

import com.biliplus.pojo.entity.AdminOperationLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface AdminOperationLogMapper {

    @Insert("INSERT INTO admin_operation_log(admin_id, action, target_type, target_id, detail, ip, create_time) " +
            "VALUES(#{adminId}, #{action}, #{targetType}, #{targetId}, #{detail}, #{ip}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(AdminOperationLog log);

    @Select("<script>" +
            "SELECT * FROM admin_operation_log WHERE 1=1 " +
            "<if test='adminId != null'> AND admin_id = #{adminId}</if> " +
            "<if test='action != null and action != \"\"'> AND action = #{action}</if> " +
            "ORDER BY id DESC LIMIT #{offset}, #{size}" +
            "</script>")
    List<AdminOperationLog> adminList(@Param("adminId") Long adminId,
                                      @Param("action") String action,
                                      @Param("offset") int offset,
                                      @Param("size") int size);

    @Select("<script>" +
            "SELECT COUNT(*) FROM admin_operation_log WHERE 1=1 " +
            "<if test='adminId != null'> AND admin_id = #{adminId}</if> " +
            "<if test='action != null and action != \"\"'> AND action = #{action}</if>" +
            "</script>")
    long adminCount(@Param("adminId") Long adminId, @Param("action") String action);

    /** 各动作计数，看板与审计都用 */
    @Select("SELECT action, COUNT(*) AS cnt FROM admin_operation_log " +
            "WHERE create_time >= #{from} GROUP BY action ORDER BY cnt DESC")
    List<Map<String, Object>> countByAction(@Param("from") java.time.LocalDateTime from);
}
