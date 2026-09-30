package com.biliplus.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface VideoFeatureMapper {

    /** 审核通过入池（重复过审幂等） */
    @Insert("INSERT INTO video_feature(video_id, duration_sec, is_original, hot_score, pool_status) " +
            "VALUES(#{videoId}, #{durationSec}, #{isOriginal}, 0, 1) " +
            "ON DUPLICATE KEY UPDATE duration_sec = VALUES(duration_sec), " +
            "is_original = VALUES(is_original), pool_status = 1")
    void enterPool(@Param("videoId") Long videoId,
                   @Param("durationSec") Integer durationSec,
                   @Param("isOriginal") Integer isOriginal);

    /** 下架 / 驳回 / 删除出池 */
    @Update("UPDATE video_feature SET pool_status = 0 WHERE video_id = #{videoId}")
    int leavePool(@Param("videoId") Long videoId);

    /** 补齐存量已上架视频入池，避免推荐/热榜空窗 */
    @Insert("INSERT IGNORE INTO video_feature(video_id, duration_sec, is_original, hot_score, pool_status) " +
            "SELECT v.id, v.duration_sec, IFNULL(v.is_original, 1), 0, 1 " +
            "FROM video v WHERE v.status = 1")
    int ensurePool();

    /** 非上架视频出池（与 video.status 对齐） */
    @Update("UPDATE video_feature f JOIN video v ON v.id = f.video_id " +
            "SET f.pool_status = 0, f.update_time = NOW() " +
            "WHERE f.pool_status = 1 AND (v.status IS NULL OR v.status <> 1)")
    int markOffline();

    /** 周期刷新热度（可由定时任务或推荐前调用） */
    @Update("UPDATE video_feature f " +
            "JOIN video v ON v.id = f.video_id " +
            "SET f.hot_score = (v.view_count + v.like_count * 3 + v.comment_count * 4 + v.share_count * 2) " +
            "  * POW(0.5, TIMESTAMPDIFF(HOUR, v.create_time, NOW()) / 72.0) " +
            "  + (CASE WHEN f.is_original = 1 THEN 5 ELSE 0 END), " +
            "f.update_time = NOW() " +
            "WHERE f.pool_status = 1")
    int refreshHotScores();
}
