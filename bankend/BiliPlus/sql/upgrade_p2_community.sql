-- ============================================================
-- P2 增量脚本：社区闭环（通知 / 播放历史 / 收藏夹 / 动态 / 举报）
-- 与 biliplus.sql 基线保持一致
-- ============================================================

SET NAMES utf8mb4;

-- ----------------------------
-- 播放历史
-- ----------------------------
DROP TABLE IF EXISTS `play_history`;
CREATE TABLE `play_history` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `video_id` bigint NOT NULL COMMENT '视频ID',
  `progress_sec` int NOT NULL DEFAULT 0 COMMENT '播放进度(秒)',
  `duration_sec` int NOT NULL DEFAULT 0 COMMENT '视频总时长(秒)',
  `last_play_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后播放时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_video`(`user_id` ASC, `video_id` ASC) USING BTREE,
  INDEX `idx_user_time`(`user_id` ASC, `last_play_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '播放历史' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 站内通知
-- ----------------------------
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint NOT NULL COMMENT '接收者',
  `type` tinyint NOT NULL COMMENT '1评论 2回复 3关注 4审核 5直播开播 6系统',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标题',
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '内容',
  `biz_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'video|comment|live|system',
  `biz_id` bigint NULL DEFAULT NULL COMMENT '关联业务ID',
  `from_user_id` bigint NULL DEFAULT NULL COMMENT '触发者',
  `is_read` tinyint NOT NULL DEFAULT 0 COMMENT '0未读 1已读',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_read`(`user_id` ASC, `is_read` ASC, `create_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '站内通知' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 举报
-- ----------------------------
DROP TABLE IF EXISTS `report`;
CREATE TABLE `report` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `reporter_id` bigint NOT NULL COMMENT '举报人',
  `target_type` tinyint NOT NULL COMMENT '1视频 2评论 3弹幕 4用户 5直播间',
  `target_id` bigint NOT NULL COMMENT '目标ID',
  `reason` tinyint NOT NULL COMMENT '1违法 2色情 3辱骂 4广告 5其他',
  `detail` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '补充说明',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '0待处理 1已处理 2已驳回',
  `handler_id` bigint NULL DEFAULT NULL COMMENT '处理管理员ID',
  `handle_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '处理备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `handle_time` datetime NULL DEFAULT NULL COMMENT '处理时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_status`(`status` ASC, `create_time` ASC) USING BTREE,
  INDEX `idx_target`(`target_type` ASC, `target_id` ASC) USING BTREE,
  INDEX `idx_reporter`(`reporter_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '举报' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 收藏夹
-- ----------------------------
DROP TABLE IF EXISTS `favorite_folder`;
CREATE TABLE `favorite_folder` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '收藏夹名称',
  `is_default` tinyint NOT NULL DEFAULT 0 COMMENT '1为默认收藏夹（不可删除）',
  `is_private` tinyint NOT NULL DEFAULT 0 COMMENT '1私密',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '收藏夹' ROW_FORMAT = Dynamic;

-- 已有收藏数据统一归入默认收藏夹（folder_id 为 NULL 时按默认夹处理）
ALTER TABLE `video_favorite`
  ADD COLUMN `folder_id` bigint NULL DEFAULT NULL COMMENT '收藏夹ID' AFTER `video_id`,
  ADD INDEX `idx_folder`(`folder_id` ASC) USING BTREE;

-- ----------------------------
-- 用户动态
-- ----------------------------
DROP TABLE IF EXISTS `dynamic`;
CREATE TABLE `dynamic` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint NOT NULL COMMENT '发布者',
  `type` tinyint NOT NULL COMMENT '1文字 2投稿视频 3转发 4开播',
  `content` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '正文',
  `video_id` bigint NULL DEFAULT NULL COMMENT '关联视频ID',
  `origin_dynamic_id` bigint NULL DEFAULT NULL COMMENT '转发的原动态ID',
  `live_room_id` bigint NULL DEFAULT NULL COMMENT '关联直播间ID',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '1正常 0删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_time`(`user_id` ASC, `create_time` ASC) USING BTREE,
  INDEX `idx_type_status`(`type` ASC, `status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户动态' ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `dynamic_like`;
CREATE TABLE `dynamic_like` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `dynamic_id` bigint NOT NULL COMMENT '动态ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_dyn`(`user_id` ASC, `dynamic_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '动态点赞' ROW_FORMAT = Dynamic;
