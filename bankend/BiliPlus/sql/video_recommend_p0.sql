-- ============================================================
-- 推荐 P0：投稿特征 + 推荐池
-- 用法：mysql -u root -p biliplus < bankend/BiliPlus/sql/video_recommend_p0.sql
-- ============================================================

-- 1) video 表补结构化特征（兼容旧数据：默认原创，时长秒可空）
ALTER TABLE `video`
    ADD COLUMN `is_original` tinyint NOT NULL DEFAULT 1 COMMENT '1原创 0转载' AFTER `category_id`,
    ADD COLUMN `duration_sec` int NULL DEFAULT NULL COMMENT '时长秒（数值）' AFTER `duration`;

-- 2) 推荐池 / 视频特征（过审入池，下架/驳回出池）
CREATE TABLE IF NOT EXISTS `video_feature` (
  `video_id` bigint NOT NULL COMMENT '视频ID',
  `duration_sec` int NULL DEFAULT NULL COMMENT '时长秒',
  `is_original` tinyint NOT NULL DEFAULT 1 COMMENT '1原创 0转载',
  `hot_score` double NOT NULL DEFAULT 0 COMMENT '热度分（可周期刷新）',
  `pool_status` tinyint NOT NULL DEFAULT 1 COMMENT '1在池 0出池',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`video_id`) USING BTREE,
  KEY `idx_pool_hot` (`pool_status`, `hot_score`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '视频推荐特征/推荐池' ROW_FORMAT = Dynamic;

-- 3) 存量已上架视频先入池，避免推荐空窗
INSERT INTO `video_feature` (`video_id`, `duration_sec`, `is_original`, `hot_score`, `pool_status`)
SELECT v.id,
       v.duration_sec,
       IFNULL(v.is_original, 1),
       v.view_count + v.like_count * 3 + v.comment_count * 4 + v.share_count * 2,
       1
FROM `video` v
WHERE v.status = 1
ON DUPLICATE KEY UPDATE `pool_status` = 1,
                        `duration_sec` = VALUES(`duration_sec`),
                        `is_original` = VALUES(`is_original`);
