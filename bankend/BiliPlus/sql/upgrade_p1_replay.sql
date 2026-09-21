-- ============================================================
-- P1 增量脚本：直播回放
-- 与 biliplus.sql 基线保持一致
-- ============================================================

SET NAMES utf8mb4;

DROP TABLE IF EXISTS `live_replay`;
CREATE TABLE `live_replay` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `live_room_id` bigint NOT NULL COMMENT '直播间ID',
  `user_id` bigint NOT NULL COMMENT '主播用户ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '回放标题',
  `cover_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '封面URL',
  `play_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '回放点播地址',
  `duration_sec` int NULL DEFAULT NULL COMMENT '时长(秒)',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '1可用 0转码中 -1删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_room`(`live_room_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '直播回放' ROW_FORMAT = Dynamic;
