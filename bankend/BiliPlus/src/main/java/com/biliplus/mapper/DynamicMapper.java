package com.biliplus.mapper;

import com.biliplus.pojo.entity.Dynamic;
import com.biliplus.pojo.vo.DynamicVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface DynamicMapper {

    /** 动态本体 + 作者 + 关联视频 + 点赞数，字段靠 map-underscore-to-camel-case 落到 DynamicVO */
    String SELECT_WITH_JOIN =
            "SELECT d.id, d.user_id, d.type, d.content, d.video_id, d.live_room_id, d.create_time, " +
            "u.nickname, u.avatar, v.title AS video_title, v.cover_url AS video_cover_url, " +
            "(SELECT COUNT(*) FROM dynamic_like dl WHERE dl.dynamic_id = d.id) AS like_count " +
            "FROM dynamic d " +
            "LEFT JOIN user u ON d.user_id = u.id " +
            "LEFT JOIN video v ON d.video_id = v.id ";

    @Insert("INSERT INTO dynamic(user_id, type, content, video_id, origin_dynamic_id, live_room_id, status, create_time) " +
            "VALUES(#{userId}, #{type}, #{content}, #{videoId}, #{originDynamicId}, #{liveRoomId}, 1, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Dynamic dynamic);

    @Select("SELECT * FROM dynamic WHERE id = #{id}")
    Dynamic selectById(@Param("id") Long id);

    /** 同一视频只自动发一条投稿动态 */
    @Select("SELECT COUNT(*) FROM dynamic WHERE type = 2 AND video_id = #{videoId}")
    long countVideoDynamic(@Param("videoId") Long videoId);

    @Select(SELECT_WITH_JOIN + "WHERE d.status = 1 ORDER BY d.id DESC LIMIT #{offset}, #{size}")
    List<DynamicVO> selectHot(@Param("offset") int offset, @Param("size") int size);

    @Select("SELECT COUNT(*) FROM dynamic WHERE status = 1")
    long countHot();

    /** 关注流：我关注的人 + 我自己 */
    @Select(SELECT_WITH_JOIN +
            "WHERE d.status = 1 AND (d.user_id = #{userId} " +
            "  OR d.user_id IN (SELECT follow_user_id FROM user_follow WHERE user_id = #{userId})) " +
            "ORDER BY d.id DESC LIMIT #{offset}, #{size}")
    List<DynamicVO> selectFeed(@Param("userId") Long userId,
                               @Param("offset") int offset,
                               @Param("size") int size);

    @Select("SELECT COUNT(*) FROM dynamic WHERE status = 1 AND (user_id = #{userId} " +
            "  OR user_id IN (SELECT follow_user_id FROM user_follow WHERE user_id = #{userId}))")
    long countFeed(@Param("userId") Long userId);

    @Select(SELECT_WITH_JOIN + "WHERE d.status = 1 AND d.user_id = #{userId} ORDER BY d.id DESC LIMIT #{offset}, #{size}")
    List<DynamicVO> selectByUser(@Param("userId") Long userId,
                                 @Param("offset") int offset,
                                 @Param("size") int size);

    @Select("SELECT COUNT(*) FROM dynamic WHERE status = 1 AND user_id = #{userId}")
    long countByUser(@Param("userId") Long userId);

    @Update("UPDATE dynamic SET status = 0 WHERE id = #{id} AND user_id = #{userId}")
    int softDelete(@Param("id") Long id, @Param("userId") Long userId);

    @Insert("INSERT INTO dynamic_like(user_id, dynamic_id, create_time) VALUES(#{userId}, #{dynamicId}, #{createTime}) " +
            "ON DUPLICATE KEY UPDATE user_id = user_id")
    void insertLike(@Param("userId") Long userId,
                    @Param("dynamicId") Long dynamicId,
                    @Param("createTime") java.time.LocalDateTime createTime);

    @Delete("DELETE FROM dynamic_like WHERE user_id = #{userId} AND dynamic_id = #{dynamicId}")
    int deleteLike(@Param("userId") Long userId, @Param("dynamicId") Long dynamicId);

    @Select("SELECT COUNT(*) FROM dynamic_like WHERE user_id = #{userId} AND dynamic_id = #{dynamicId}")
    int countLike(@Param("userId") Long userId, @Param("dynamicId") Long dynamicId);

    @Select("SELECT COUNT(*) FROM dynamic_like WHERE dynamic_id = #{dynamicId}")
    long countLikes(@Param("dynamicId") Long dynamicId);

    @Select("<script>" +
            "SELECT dynamic_id FROM dynamic_like WHERE user_id = #{userId} AND dynamic_id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>" +
            "</script>")
    List<Long> selectLikedIds(@Param("userId") Long userId, @Param("ids") List<Long> ids);
}
