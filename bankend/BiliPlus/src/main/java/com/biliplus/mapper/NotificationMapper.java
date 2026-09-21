package com.biliplus.mapper;

import com.biliplus.pojo.entity.Notification;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface NotificationMapper {

    @Insert("INSERT INTO notification(user_id, type, title, content, biz_type, biz_id, from_user_id, is_read, create_time) " +
            "VALUES(#{userId}, #{type}, #{title}, #{content}, #{bizType}, #{bizId}, #{fromUserId}, 0, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Notification notification);

    @Select("<script>" +
            "SELECT * FROM notification WHERE user_id = #{userId} " +
            "<if test='isRead != null'> AND is_read = #{isRead}</if> " +
            "<if test='type != null'> AND type = #{type}</if> " +
            "ORDER BY id DESC LIMIT #{offset}, #{size}" +
            "</script>")
    List<Notification> pageByUser(@Param("userId") Long userId,
                                  @Param("isRead") Integer isRead,
                                  @Param("type") Integer type,
                                  @Param("offset") int offset,
                                  @Param("size") int size);

    @Select("<script>" +
            "SELECT COUNT(*) FROM notification WHERE user_id = #{userId} " +
            "<if test='isRead != null'> AND is_read = #{isRead}</if> " +
            "<if test='type != null'> AND type = #{type}</if>" +
            "</script>")
    long countByUser(@Param("userId") Long userId,
                     @Param("isRead") Integer isRead,
                     @Param("type") Integer type);

    @Select("SELECT COUNT(*) FROM notification WHERE user_id = #{userId} AND is_read = 0")
    long countUnread(@Param("userId") Long userId);

    /** 只能标记自己的通知，越权时影响行数为 0 */
    @Update("UPDATE notification SET is_read = 1 WHERE id = #{id} AND user_id = #{userId}")
    int markRead(@Param("id") Long id, @Param("userId") Long userId);

    @Update("UPDATE notification SET is_read = 1 WHERE user_id = #{userId} AND is_read = 0")
    int markAllRead(@Param("userId") Long userId);
}
