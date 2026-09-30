/*
 Navicat Premium Data Transfer

 Source Server         : bycg
 Source Server Type    : MySQL
 Source Server Version : 80042 (8.0.42)
 Source Host           : localhost:3306
 Source Schema         : biliplus

 Target Server Type    : MySQL
 Target Server Version : 80042 (8.0.42)
 File Encoding         : 65001

 Date: 21/09/2026 19:30:40
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for admin_user
-- ----------------------------
DROP TABLE IF EXISTS `admin_user`;
CREATE TABLE `admin_user`  (
  `id` int NOT NULL,
  `account` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`, `name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for anime
-- ----------------------------
DROP TABLE IF EXISTS `anime`;
CREATE TABLE `anime`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '鐣墽ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '鐣墽鏍囬',
  `cover_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '灏侀潰URL',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '鐣墽鎻忚堪',
  `total_episodes` int NOT NULL DEFAULT 0 COMMENT '鎬婚泦鏁?,
  `aired_episodes` int NOT NULL DEFAULT 0 COMMENT '宸叉挱鍑洪泦鏁?,
  `status` tinyint NOT NULL COMMENT '鐘舵€侊細0-鏈挱鍑猴紝1-杩炶浇涓紝2-宸插畬缁?,
  `score` decimal(2, 1) NULL DEFAULT NULL COMMENT '璇勫垎',
  `follower_count` int NOT NULL DEFAULT 0 COMMENT '杩界暘鏁?,
  `release_time` date NULL DEFAULT NULL COMMENT '涓婃槧鏃堕棿',
  `end_time` date NULL DEFAULT NULL COMMENT '瀹岀粨鏃堕棿',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_score`(`score` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '鐣墽琛? ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for anime_episode
-- ----------------------------
DROP TABLE IF EXISTS `anime_episode`;
CREATE TABLE `anime_episode`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '鍓ч泦ID',
  `anime_id` bigint NOT NULL COMMENT '鐣墽ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '鍓ч泦鏍囬',
  `episode_num` int NOT NULL COMMENT '闆嗘暟',
  `video_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '瑙嗛URL',
  `duration` int NOT NULL COMMENT '鏃堕暱(绉?',
  `view_count` int NOT NULL DEFAULT 0 COMMENT '鎾斁閲?,
  `release_time` datetime NULL DEFAULT NULL COMMENT '鍙戝竷鏃堕棿',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_anime_id`(`anime_id` ASC) USING BTREE,
  INDEX `idx_episode_num`(`episode_num` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '鐣墽鍓ч泦琛? ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for banner
-- ----------------------------
DROP TABLE IF EXISTS `banner`;
CREATE TABLE `banner`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '鏍囬',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '鎻忚堪',
  `image_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '鍥剧墖URL',
  `link_type` tinyint NOT NULL DEFAULT 1 COMMENT '璺宠浆绫诲瀷锛?-瑙嗛 2-澶栭摼 3-涓嶈烦杞?,
  `video_id` bigint NULL DEFAULT NULL COMMENT '鍏宠仈瑙嗛ID锛坙ink_type=1锛?,
  `link_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '澶栭摼锛坙ink_type=2锛?,
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '鎺掑簭锛岃秺灏忚秺闈犲墠',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '鐘舵€侊細0-涓嬬嚎 1-涓婄嚎',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_status_sort`(`status` ASC, `sort_order` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '棣栭〉杞挱鍥捐〃' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for category
-- ----------------------------
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '鍒嗙被ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '鍒嗙被鍚嶇О',
  `parent_id` int NOT NULL DEFAULT 0 COMMENT '鐖跺垎绫籌D',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '鎺掑簭',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '鍥炬爣URL',
  `type` tinyint NOT NULL DEFAULT 1 COMMENT '1瑙嗛鍒嗗尯 2鐩存挱鍒嗗尯',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE,
  INDEX `idx_type`(`type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 20 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '鍒嗙被琛? ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for chat_conversation
-- ----------------------------
DROP TABLE IF EXISTS `chat_conversation`;
CREATE TABLE `chat_conversation`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '浼氳瘽ID',
  `type` tinyint NOT NULL DEFAULT 1 COMMENT '浼氳瘽绫诲瀷锛?-绉佽亰锛?-缇よ亰',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '缇よ亰鍚嶇О锛堢鑱婃椂涓篘ULL锛?,
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '缇ゅご鍍忥紙绉佽亰鏃朵负NULL锛?,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_type`(`type` ASC) USING BTREE,
  INDEX `idx_update_time`(`update_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '鑱婂ぉ浼氳瘽琛? ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for chat_conversation_member
-- ----------------------------
DROP TABLE IF EXISTS `chat_conversation_member`;
CREATE TABLE `chat_conversation_member`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `conversation_id` bigint NOT NULL COMMENT '浼氳瘽ID',
  `user_id` bigint NOT NULL COMMENT '鐢ㄦ埛ID',
  `unread_count` int NOT NULL DEFAULT 0 COMMENT '鏈娑堟伅鏁帮紙鎸佷箙鍖栭儴鍒嗭級',
  `last_read_msg_id` bigint NULL DEFAULT NULL COMMENT '鏈€鍚庡凡璇荤殑娑堟伅ID',
  `join_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍔犲叆鏃堕棿',
  `leave_time` datetime NULL DEFAULT NULL COMMENT '閫€鍑烘椂闂达紙NULL琛ㄧず鏈€€鍑猴級',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_conv_user`(`conversation_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_conv_id`(`conversation_id` ASC) USING BTREE,
  CONSTRAINT `fk_cmem_conv` FOREIGN KEY (`conversation_id`) REFERENCES `chat_conversation` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_cmem_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 25 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '浼氳瘽鎴愬憳琛? ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for chat_message
-- ----------------------------
DROP TABLE IF EXISTS `chat_message`;
CREATE TABLE `chat_message`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '娑堟伅ID',
  `conversation_id` bigint NOT NULL COMMENT '鎵€灞炰細璇滻D',
  `sender_id` bigint NOT NULL COMMENT '鍙戦€佽€呯敤鎴稩D',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '娑堟伅鍐呭锛圝SON鏍煎紡锛屾敮鎸佸瘜鏂囨湰锛?,
  `msg_type` tinyint NOT NULL DEFAULT 1 COMMENT '娑堟伅绫诲瀷锛?-鏂囨湰锛?-鍥剧墖锛?-閾炬帴锛?-琛ㄦ儏锛?-鏂囦欢',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_conv_create`(`conversation_id` ASC, `create_time` ASC) USING BTREE,
  INDEX `idx_sender`(`sender_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  CONSTRAINT `fk_msg_conv` FOREIGN KEY (`conversation_id`) REFERENCES `chat_conversation` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_msg_sender` FOREIGN KEY (`sender_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 79 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '鑱婂ぉ娑堟伅琛? ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for comment
-- ----------------------------
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '璇勮ID',
  `video_id` bigint NOT NULL COMMENT '瑙嗛ID',
  `user_id` bigint NOT NULL COMMENT '鐢ㄦ埛ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '璇勮鍐呭',
  `parent_id` bigint NULL DEFAULT 0 COMMENT '鐖惰瘎璁篒D锛?琛ㄧず椤剁骇璇勮',
  `like_count` int NOT NULL DEFAULT 0 COMMENT '鐐硅禐鏁?,
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '鐘舵€侊細0-鍒犻櫎锛?-姝ｅ父',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_video_id`(`video_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 47 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '璇勮琛? ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for danmaku
-- ----------------------------
DROP TABLE IF EXISTS `danmaku`;
CREATE TABLE `danmaku`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '寮瑰箷ID',
  `video_id` bigint NOT NULL COMMENT '瑙嗛ID',
  `user_id` bigint NOT NULL COMMENT '鐢ㄦ埛ID',
  `content` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '寮瑰箷鍐呭',
  `time` int NOT NULL COMMENT '鍑虹幇鏃堕棿(绉?',
  `color` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'FFFFFF' COMMENT '棰滆壊(鍗佸叚杩涘埗)',
  `type` tinyint NOT NULL DEFAULT 1 COMMENT '绫诲瀷锛?-婊氬姩锛?-椤堕儴锛?-搴曢儴',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '鐘舵€侊細0-灞忚斀锛?-姝ｅ父',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_video_id`(`video_id` ASC) USING BTREE,
  INDEX `idx_video_time`(`video_id` ASC, `time` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 38 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '寮瑰箷琛? ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for dynamic
-- ----------------------------
DROP TABLE IF EXISTS `dynamic`;
CREATE TABLE `dynamic`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint NOT NULL COMMENT '鍙戝竷鑰?,
  `type` tinyint NOT NULL COMMENT '1鏂囧瓧 2鎶曠瑙嗛 3杞彂 4寮€鎾?,
  `content` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '姝ｆ枃',
  `video_id` bigint NULL DEFAULT NULL COMMENT '鍏宠仈瑙嗛ID',
  `origin_dynamic_id` bigint NULL DEFAULT NULL COMMENT '杞彂鐨勫師鍔ㄦ€両D',
  `live_room_id` bigint NULL DEFAULT NULL COMMENT '鍏宠仈鐩存挱闂碔D',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '1姝ｅ父 0鍒犻櫎',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_time`(`user_id` ASC, `create_time` ASC) USING BTREE,
  INDEX `idx_type_status`(`type` ASC, `status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '鐢ㄦ埛鍔ㄦ€? ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for dynamic_like
-- ----------------------------
DROP TABLE IF EXISTS `dynamic_like`;
CREATE TABLE `dynamic_like`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint NOT NULL COMMENT '鐢ㄦ埛ID',
  `dynamic_id` bigint NOT NULL COMMENT '鍔ㄦ€両D',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_dyn`(`user_id` ASC, `dynamic_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '鍔ㄦ€佺偣璧? ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for favorite_folder
-- ----------------------------
DROP TABLE IF EXISTS `favorite_folder`;
CREATE TABLE `favorite_folder`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint NOT NULL COMMENT '鐢ㄦ埛ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '鏀惰棌澶瑰悕绉?,
  `is_default` tinyint NOT NULL DEFAULT 0 COMMENT '1涓洪粯璁ゆ敹钘忓す锛堜笉鍙垹闄わ級',
  `is_private` tinyint NOT NULL DEFAULT 0 COMMENT '1绉佸瘑',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '鏀惰棌澶? ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for gift
-- ----------------------------
DROP TABLE IF EXISTS `gift`;
CREATE TABLE `gift`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `icon_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `price` int NOT NULL COMMENT '纭竵浠锋牸',
  `effect_level` tinyint NOT NULL DEFAULT 1 COMMENT '1鏅€?2涓瓑 3鍏ㄥ睆',
  `sort_order` int NOT NULL DEFAULT 0,
  `status` tinyint NOT NULL DEFAULT 1,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 38 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '绀肩墿鐩綍' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for gift_record
-- ----------------------------
DROP TABLE IF EXISTS `gift_record`;
CREATE TABLE `gift_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `live_room_id` bigint NOT NULL,
  `pk_id` bigint NULL DEFAULT NULL COMMENT '鑻ュ湪PK涓垯璁″叆姣斿垎',
  `gift_id` bigint NOT NULL,
  `sender_id` bigint NOT NULL,
  `host_user_id` bigint NOT NULL,
  `unit_price` int NOT NULL,
  `count` int NOT NULL DEFAULT 1,
  `total_price` int NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_room`(`live_room_id` ASC) USING BTREE,
  INDEX `idx_sender`(`sender_id` ASC) USING BTREE,
  INDEX `idx_host`(`host_user_id` ASC) USING BTREE,
  INDEX `idx_pk`(`pk_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '鎵撹祻娴佹按' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for host_income
-- ----------------------------
DROP TABLE IF EXISTS `host_income`;
CREATE TABLE `host_income`  (
  `user_id` bigint NOT NULL,
  `total_income` bigint NOT NULL DEFAULT 0,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '涓绘挱鏀剁泭姹囨€伙紙鎻愮幇鍚庣画锛? ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for live_mic_session
-- ----------------------------
DROP TABLE IF EXISTS `live_mic_session`;
CREATE TABLE `live_mic_session`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `live_room_id` bigint NOT NULL,
  `host_user_id` bigint NOT NULL,
  `guest_user_id` bigint NOT NULL,
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '0鐢宠 1杩涜涓?2缁撴潫 3鎷掔粷',
  `start_time` datetime NULL DEFAULT NULL,
  `end_time` datetime NULL DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_room`(`live_room_id` ASC) USING BTREE,
  INDEX `idx_guest`(`guest_user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '鐩存挱杩為害浼氳瘽' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for live_pk
-- ----------------------------
DROP TABLE IF EXISTS `live_pk`;
CREATE TABLE `live_pk`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `room_a_id` bigint NOT NULL,
  `room_b_id` bigint NOT NULL,
  `host_a_id` bigint NOT NULL,
  `host_b_id` bigint NOT NULL,
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '0閭€璇?1杩涜 2缁撴潫 3鍙栨秷',
  `score_a` int NOT NULL DEFAULT 0,
  `score_b` int NOT NULL DEFAULT 0,
  `start_time` datetime NULL DEFAULT NULL,
  `end_time` datetime NULL DEFAULT NULL,
  `duration_sec` int NOT NULL DEFAULT 300,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_room_a`(`room_a_id` ASC) USING BTREE,
  INDEX `idx_room_b`(`room_b_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '鐩存挱PK' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for live_replay
-- ----------------------------
DROP TABLE IF EXISTS `live_replay`;
CREATE TABLE `live_replay`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `live_room_id` bigint NOT NULL COMMENT '鐩存挱闂碔D',
  `user_id` bigint NOT NULL COMMENT '涓绘挱鐢ㄦ埛ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '鍥炴斁鏍囬',
  `cover_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '灏侀潰URL',
  `play_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '鍥炴斁鐐规挱鍦板潃',
  `duration_sec` int NULL DEFAULT NULL COMMENT '鏃堕暱(绉?',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '1鍙敤 0杞爜涓?-1鍒犻櫎',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_room`(`live_room_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '鐩存挱鍥炴斁' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for live_room
-- ----------------------------
DROP TABLE IF EXISTS `live_room`;
CREATE TABLE `live_room`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '鐩存挱闂碔D',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '鐩存挱鏍囬',
  `cover_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '灏侀潰URL',
  `user_id` bigint NOT NULL COMMENT '涓绘挱鐢ㄦ埛ID',
  `category_id` int NOT NULL COMMENT '鍒嗙被ID',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '鐘舵€侊細0-鏈紑鎾紝1-鐩存挱涓紝2-宸茬粨鏉?,
  `view_count` int NOT NULL DEFAULT 0 COMMENT '瑙傜湅浜烘暟',
  `start_time` datetime NULL DEFAULT NULL COMMENT '寮€濮嬫椂闂?,
  `end_time` datetime NULL DEFAULT NULL COMMENT '缁撴潫鏃堕棿',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
  `stream_key` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '鎺ㄦ祦瀵嗛挜',
  `play_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'HTTP-FLV 鎷夋祦鍦板潃',
  `push_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'RTMP 鎺ㄦ祦鍦板潃',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_stream_key`(`stream_key` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_category_id`(`category_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '鐩存挱闂磋〃' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for live_user
-- ----------------------------
DROP TABLE IF EXISTS `live_user`;
CREATE TABLE `live_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `live_room_id` bigint NOT NULL COMMENT '鐩存挱闂碔D',
  `user_id` bigint NOT NULL COMMENT '鐢ㄦ埛ID',
  `enter_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '杩涘叆鏃堕棿',
  `leave_time` datetime NULL DEFAULT NULL COMMENT '绂诲紑鏃堕棿',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_live_user`(`live_room_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '鐩存挱瑙備紬琛? ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for notification
-- ----------------------------
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint NOT NULL COMMENT '鎺ユ敹鑰?,
  `type` tinyint NOT NULL COMMENT '1璇勮 2鍥炲 3鍏虫敞 4瀹℃牳 5鐩存挱寮€鎾?6绯荤粺',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '鏍囬',
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '鍐呭',
  `biz_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'video|comment|live|system',
  `biz_id` bigint NULL DEFAULT NULL COMMENT '鍏宠仈涓氬姟ID',
  `from_user_id` bigint NULL DEFAULT NULL COMMENT '瑙﹀彂鑰?,
  `is_read` tinyint NOT NULL DEFAULT 0 COMMENT '0鏈 1宸茶',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_read`(`user_id` ASC, `is_read` ASC, `create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '绔欏唴閫氱煡' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for play_history
-- ----------------------------
DROP TABLE IF EXISTS `play_history`;
CREATE TABLE `play_history`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint NOT NULL COMMENT '鐢ㄦ埛ID',
  `video_id` bigint NOT NULL COMMENT '瑙嗛ID',
  `progress_sec` int NOT NULL DEFAULT 0 COMMENT '鎾斁杩涘害(绉?',
  `duration_sec` int NOT NULL DEFAULT 0 COMMENT '瑙嗛鎬绘椂闀?绉?',
  `last_play_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鏈€鍚庢挱鏀炬椂闂?,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_video`(`user_id` ASC, `video_id` ASC) USING BTREE,
  INDEX `idx_user_time`(`user_id` ASC, `last_play_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '鎾斁鍘嗗彶' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for recharge_order
-- ----------------------------
DROP TABLE IF EXISTS `recharge_order`;
CREATE TABLE `recharge_order`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `order_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '涓氬姟璁㈠崟鍙?,
  `user_id` bigint NOT NULL COMMENT '鐢ㄦ埛ID',
  `amount` bigint NOT NULL COMMENT '纭竵鏁伴噺',
  `pay_amount` decimal(10, 2) NOT NULL COMMENT '搴斾粯閲戦',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '0寰呮敮浠?1宸叉敮浠?2宸插彇娑?,
  `pay_channel` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'mock|alipay|wechat',
  `paid_time` datetime NULL DEFAULT NULL COMMENT '鏀粯鏃堕棿',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_order_no`(`order_no` ASC) USING BTREE,
  INDEX `idx_user_status`(`user_id` ASC, `status` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '鍏呭€艰鍗? ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for report
-- ----------------------------
DROP TABLE IF EXISTS `report`;
CREATE TABLE `report`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `reporter_id` bigint NOT NULL COMMENT '涓炬姤浜?,
  `target_type` tinyint NOT NULL COMMENT '1瑙嗛 2璇勮 3寮瑰箷 4鐢ㄦ埛 5鐩存挱闂?,
  `target_id` bigint NOT NULL COMMENT '鐩爣ID',
  `reason` tinyint NOT NULL COMMENT '1杩濇硶 2鑹叉儏 3杈遍獋 4骞垮憡 5鍏朵粬',
  `detail` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '琛ュ厖璇存槑',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '0寰呭鐞?1宸插鐞?2宸查┏鍥?,
  `handler_id` bigint NULL DEFAULT NULL COMMENT '澶勭悊绠＄悊鍛業D',
  `handle_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '澶勭悊澶囨敞',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  `handle_time` datetime NULL DEFAULT NULL COMMENT '澶勭悊鏃堕棿',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_status`(`status` ASC, `create_time` ASC) USING BTREE,
  INDEX `idx_target`(`target_type` ASC, `target_id` ASC) USING BTREE,
  INDEX `idx_reporter`(`reporter_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '涓炬姤' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for tag
-- ----------------------------
DROP TABLE IF EXISTS `tag`;
CREATE TABLE `tag`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '鏍囩ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '鏍囩鍚嶇О',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_name`(`name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '鏍囩琛? ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '鐢ㄦ埛ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '鐢ㄦ埛鍚?,
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '鍔犲瘑瀵嗙爜',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '閭',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '鎵嬫満鍙?,
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '澶村儚URL',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '鏄电О',
  `signature` varchar(255) CHARACTER SET utf16le COLLATE utf16le_general_ci NULL DEFAULT NULL COMMENT '涓€х鍚?,
  `role` tinyint NOT NULL DEFAULT 0 COMMENT '瑙掕壊锛?-鏅€氱敤鎴凤紝1-UP涓伙紝2-绠＄悊鍛?,
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '鐘舵€侊細0-绂佺敤锛?-姝ｅ父',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_username`(`username` ASC) USING BTREE,
  INDEX `idx_email`(`email` ASC) USING BTREE,
  INDEX `idx_phone`(`phone` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '鐢ㄦ埛琛? ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_anime_follow
-- ----------------------------
DROP TABLE IF EXISTS `user_anime_follow`;
CREATE TABLE `user_anime_follow`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint NOT NULL COMMENT '鐢ㄦ埛ID',
  `anime_id` bigint NOT NULL COMMENT '鐣墽ID',
  `latest_episode` int NOT NULL DEFAULT 0 COMMENT '鏈€鏂拌鐪嬮泦鏁?,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '杩界暘鏃堕棿',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_user_anime`(`user_id` ASC, `anime_id` ASC) USING BTREE,
  INDEX `idx_anime_id`(`anime_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '鐢ㄦ埛杩界暘琛? ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_follow
-- ----------------------------
DROP TABLE IF EXISTS `user_follow`;
CREATE TABLE `user_follow`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint NOT NULL COMMENT '鐢ㄦ埛ID',
  `follow_user_id` bigint NOT NULL COMMENT '琚叧娉ㄧ敤鎴稩D',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍏虫敞鏃堕棿',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_user_follow`(`user_id` ASC, `follow_user_id` ASC) USING BTREE,
  INDEX `idx_follow_user`(`follow_user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '鐢ㄦ埛鍏虫敞琛? ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_like
-- ----------------------------
DROP TABLE IF EXISTS `user_like`;
CREATE TABLE `user_like`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint NOT NULL COMMENT '鐢ㄦ埛ID',
  `target_id` bigint NOT NULL COMMENT '鐩爣ID',
  `target_type` tinyint NOT NULL COMMENT '鐩爣绫诲瀷锛?-瑙嗛锛?-璇勮',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鐐硅禐鏃堕棿',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_user_target`(`user_id` ASC, `target_id` ASC, `target_type` ASC) USING BTREE,
  INDEX `idx_target`(`target_id` ASC, `target_type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '鐢ㄦ埛鐐硅禐琛? ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_settings
-- ----------------------------
DROP TABLE IF EXISTS `user_settings`;
CREATE TABLE `user_settings`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '鐢ㄦ埛ID',
  `settings_json` json NOT NULL COMMENT '鍋忓ソ閰嶇疆 JSON',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_settings_user`(`user_id` ASC) USING BTREE,
  CONSTRAINT `fk_user_settings_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '鐢ㄦ埛鍋忓ソ璁剧疆' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for user_statistics
-- ----------------------------
DROP TABLE IF EXISTS `user_statistics`;
CREATE TABLE `user_statistics`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint NOT NULL COMMENT '鐢ㄦ埛ID',
  `follower_count` int NOT NULL DEFAULT 0 COMMENT '绮変笣鏁?,
  `following_count` int NOT NULL DEFAULT 0 COMMENT '鍏虫敞鏁?,
  `video_count` int NOT NULL DEFAULT 0 COMMENT '瑙嗛鏁?,
  `like_count` int NOT NULL DEFAULT 0 COMMENT '鑾疯禐鏁?,
  `view_count` int NOT NULL DEFAULT 0 COMMENT '鎬绘挱鏀鹃噺',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '鐢ㄦ埛缁熻淇℃伅琛? ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_wallet
-- ----------------------------
DROP TABLE IF EXISTS `user_wallet`;
CREATE TABLE `user_wallet`  (
  `user_id` bigint NOT NULL,
  `balance` bigint NOT NULL DEFAULT 0,
  `version` int NOT NULL DEFAULT 0,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '鐢ㄦ埛閽卞寘锛堢‖甯侊級' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for video
-- ----------------------------
DROP TABLE IF EXISTS `video`;
CREATE TABLE `video`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '瑙嗛ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '瑙嗛鏍囬',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '瑙嗛鎻忚堪',
  `cover_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '灏侀潰URL',
  `video_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '瑙嗛URL',
  `duration` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '瑙嗛鏃堕暱',
  `user_id` bigint NOT NULL COMMENT '涓婁紶鐢ㄦ埛ID',
  `category_id` int NOT NULL COMMENT '鍒嗙被ID',
  `is_original` tinyint NOT NULL DEFAULT 1 COMMENT '1鍘熷垱 0杞浇锛堟帹鑽愮壒寰侊級',
  `duration_sec` int NULL DEFAULT NULL COMMENT '鏃堕暱绉掞紙鏁板€硷紝鎺ㄨ崘鐢級',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '鐘舵€侊細0-寰呭鏍革紝1-姝ｅ父锛?-涓嬫灦',
  `view_count` int NOT NULL DEFAULT 0 COMMENT '鎾斁閲?,
  `like_count` int NOT NULL DEFAULT 0 COMMENT '鐐硅禐鏁?,
  `comment_count` int NOT NULL DEFAULT 0 COMMENT '璇勮鏁?,
  `share_count` int NOT NULL DEFAULT 0 COMMENT '鍒嗕韩鏁?,
  `reject_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '瀹℃牳涓嶉€氳繃鍘熷洜',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_category_id`(`category_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  INDEX `idx_view_count`(`view_count` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 52 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '瑙嗛琛? ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for video_favorite
-- ----------------------------
DROP TABLE IF EXISTS `video_favorite`;
CREATE TABLE `video_favorite`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `video_id` bigint NOT NULL COMMENT '瑙嗛ID',
  `folder_id` bigint NULL DEFAULT NULL COMMENT '鏀惰棌澶笽D',
  `user_id` bigint NOT NULL COMMENT '鐢ㄦ埛ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鏀惰棌鏃堕棿',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_video_user`(`video_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_folder`(`folder_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '瑙嗛鏀惰棌琛? ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for video_tag
-- ----------------------------
DROP TABLE IF EXISTS `video_tag`;
CREATE TABLE `video_tag`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `video_id` bigint NOT NULL COMMENT '瑙嗛ID',
  `tag_id` bigint NOT NULL COMMENT '鏍囩ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_video_tag`(`video_id` ASC, `tag_id` ASC) USING BTREE,
  INDEX `idx_tag_id`(`tag_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '瑙嗛鏍囩鍏宠仈琛? ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for wallet_transaction
-- ----------------------------
DROP TABLE IF EXISTS `wallet_transaction`;
CREATE TABLE `wallet_transaction`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint NOT NULL COMMENT '鐢ㄦ埛ID',
  `type` tinyint NOT NULL COMMENT '1鍏呭€?2閫佺ぜ鏀嚭 3涓绘挱鏀跺叆 4绯荤粺璋冩暣',
  `amount` bigint NOT NULL COMMENT '鍙樺姩閲戦锛屾涓哄叆璐︼紝璐熶负鍑鸿处',
  `balance_after` bigint NOT NULL COMMENT '鍙樺姩鍚庝綑棰濓紝鍙ｅ緞瑙?biz_type',
  `biz_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'recharge|gift|host_income|adjust',
  `biz_id` bigint NULL DEFAULT NULL COMMENT '鍏宠仈涓氬姟ID锛氬厖鍊艰鍗旾D / 鎵撹祻娴佹按ID',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '澶囨敞',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_time`(`user_id` ASC, `create_time` ASC) USING BTREE,
  INDEX `idx_biz`(`biz_type` ASC, `biz_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '閽卞寘璐﹀彉娴佹按' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sensitive_word 锛堥樁娈? 鍐呭娌荤悊锛氭晱鎰熻瘝搴擄級
-- level: 1鎷︽埅 2杞汉宸?鏍囪鍚庢斁琛? 3浠呮爣璁?
-- ----------------------------
DROP TABLE IF EXISTS `sensitive_word`;
CREATE TABLE `sensitive_word`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `word` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '鏁忔劅璇?,
  `level` tinyint NOT NULL DEFAULT 1 COMMENT '1鎷︽埅 2杞汉宸?3浠呮爣璁?,
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '1鍚敤 0鍋滅敤',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_word`(`word` ASC) USING BTREE,
  INDEX `idx_status_level`(`status` ASC, `level` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '鏁忔劅璇嶅簱' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sensitive_hit_log 锛堟晱鎰熻瘝鍛戒腑璁板綍锛岀敤浜庤浼ょ巼澶嶆牳锛?
-- review_status: 0寰呭鏍?1纭杩濊 2璇激
-- ----------------------------
DROP TABLE IF EXISTS `sensitive_hit_log`;
CREATE TABLE `sensitive_hit_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `word_id` bigint NULL DEFAULT NULL COMMENT '鏁忔劅璇岻D',
  `word` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '鍛戒腑璇?,
  `level` tinyint NOT NULL DEFAULT 1 COMMENT '鍛戒腑鏃剁殑绾у埆',
  `user_id` bigint NULL DEFAULT NULL COMMENT '鍙戝竷鑰?,
  `target_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'video|comment|danmaku|dynamic',
  `target_id` bigint NULL DEFAULT NULL COMMENT '鐩爣ID',
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '鍛戒腑鐗囨',
  `action` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'block|mark',
  `review_status` tinyint NOT NULL DEFAULT 0 COMMENT '0寰呭鏍?1纭杩濊 2璇激',
  `review_admin_id` bigint NULL DEFAULT NULL COMMENT '澶嶆牳绠＄悊鍛?,
  `review_time` datetime NULL DEFAULT NULL COMMENT '澶嶆牳鏃堕棿',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_time`(`user_id` ASC, `create_time` ASC) USING BTREE,
  INDEX `idx_target`(`target_type` ASC, `target_id` ASC) USING BTREE,
  INDEX `idx_review`(`review_status` ASC, `create_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '鏁忔劅璇嶅懡涓褰? ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_credit 锛堢敤鎴蜂俊鐢ㄥ垎锛?
-- 鍒濆 100 鍒嗭紱涓炬姤鎴愮珛 -10锛屾晱鎰熻瘝鎷︽埅 -5锛屽鏍搁┏鍥?-3锛屾姇绋块€氳繃 +2
-- ----------------------------
DROP TABLE IF EXISTS `user_credit`;
CREATE TABLE `user_credit`  (
  `user_id` bigint NOT NULL COMMENT '鐢ㄦ埛ID',
  `score` int NOT NULL DEFAULT 100 COMMENT '淇＄敤鍒?,
  `violation_count` int NOT NULL DEFAULT 0 COMMENT '杩濊娆℃暟',
  `last_violation_time` datetime NULL DEFAULT NULL COMMENT '鏈€杩戣繚瑙勬椂闂?,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
  PRIMARY KEY (`user_id`) USING BTREE,
  INDEX `idx_score`(`score` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '鐢ㄦ埛淇＄敤鍒? ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_penalty 锛堝缃褰曪紝鏀寔鏃堕檺锛?
-- action: mute 绂佽█ / ban 灏佺锛沞nd_time NULL 琛ㄧず姘镐箙
-- ----------------------------
DROP TABLE IF EXISTS `user_penalty`;
CREATE TABLE `user_penalty`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint NOT NULL COMMENT '琚缃敤鎴?,
  `action` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'mute|ban',
  `reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '鍘熷洜',
  `start_time` datetime NOT NULL COMMENT '寮€濮嬫椂闂?,
  `end_time` datetime NULL DEFAULT NULL COMMENT '鍒版湡鏃堕棿锛孨ULL=姘镐箙',
  `admin_id` bigint NULL DEFAULT NULL COMMENT '鎿嶄綔绠＄悊鍛?,
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '1鐢熸晥涓?2宸插埌鏈?3宸叉彁鍓嶈В闄?,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_status`(`user_id` ASC, `status` ASC) USING BTREE,
  INDEX `idx_expiry`(`status` ASC, `end_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '鐢ㄦ埛澶勭疆璁板綍' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for event_log 锛堥樁娈? 缁熶竴琛屼负娴佹按锛氭帹鑽愮壒寰?娌荤悊鎶ヨ〃/鍗忓悓瀹為獙鐨勫叡鍚屽師鏂欙級
-- 杩濊鏇濆厜鐜?= 绐楀彛鍐?video_view 涓?target 鍛戒腑杩濊瑙嗛闆嗗悎鐨勫崰姣?
-- extra 瀛?JSON 鏂囨湰锛堝垎绫籌D銆佺ぜ鐗╅噾棰濄€佷妇鎶ュ師鍥犵瓑鎵╁睍锛夛紝鐢?VARCHAR 閬垮厤绫诲瀷澶勭悊鍣ㄥ紑閿€
-- ----------------------------
DROP TABLE IF EXISTS `event_log`;
CREATE TABLE `event_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '鐢ㄦ埛ID锛屾湭鐧诲綍涓?NULL',
  `event_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'video_view|video_like|video_favorite|comment|danmaku|follow|gift_send|live_enter|live_watch|report|audit_pass|...',
  `target_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'video|user|comment|live_room|gift|dynamic',
  `target_id` bigint NULL DEFAULT NULL COMMENT '鐩爣ID',
  `duration_sec` int NULL DEFAULT NULL COMMENT '瑙傜湅/鐩存挱鏃堕暱(绉?',
  `extra` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'JSON 鏂囨湰鎵╁睍',
  `source` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'web|admin|system',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_time`(`user_id` ASC, `create_time` ASC) USING BTREE,
  INDEX `idx_type_target`(`event_type` ASC, `target_type` ASC, `target_id` ASC) USING BTREE,
  INDEX `idx_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '缁熶竴琛屼负娴佹按' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for admin_operation_log 锛堥樁娈? 鎿嶄綔鏃ュ織锛氱鐞嗙鍙拷婧級
-- action 褰㈠ video.approve|user.ban|report.handle|sensitive.create
-- ----------------------------
DROP TABLE IF EXISTS `admin_operation_log`;
CREATE TABLE `admin_operation_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `admin_id` bigint NULL DEFAULT NULL COMMENT '鎿嶄綔绠＄悊鍛橈紝绯荤粺鑷姩鍔ㄤ綔涓?NULL',
  `action` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'video.approve|user.ban|report.handle|...',
  `target_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'video|user|report|gift|live_room|sensitive_word',
  `target_id` bigint NULL DEFAULT NULL COMMENT '鐩爣ID',
  `detail` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '琛ュ厖璇存槑',
  `ip` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '鎿嶄綔鏉ユ簮 IP',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_admin_time`(`admin_id` ASC, `create_time` ASC) USING BTREE,
  INDEX `idx_action`(`action` ASC) USING BTREE,
  INDEX `idx_target`(`target_type` ASC, `target_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '绠＄悊绔搷浣滄棩蹇? ROW_FORMAT = Dynamic;
-- ----------------------------
-- Table structure for video_feature (recommend pool / hot score)
-- enter pool on audit pass, leave on offline; hot_score refreshed periodically
-- ----------------------------
DROP TABLE IF EXISTS `video_feature`;
CREATE TABLE `video_feature` (
  `video_id` bigint NOT NULL COMMENT 'video id',
  `duration_sec` int NULL DEFAULT NULL COMMENT 'duration seconds',
  `is_original` tinyint NOT NULL DEFAULT 1 COMMENT '1 original 0 reprint',
  `hot_score` double NOT NULL DEFAULT 0 COMMENT 'hot score',
  `pool_status` tinyint NOT NULL DEFAULT 1 COMMENT '1 in pool 0 out',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
  PRIMARY KEY (`video_id`) USING BTREE,
  KEY `idx_pool_hot` (`pool_status`, `hot_score`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci
  COMMENT = 'video recommend feature pool' ROW_FORMAT = Dynamic;

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

SET FOREIGN_KEY_CHECKS = 1;
