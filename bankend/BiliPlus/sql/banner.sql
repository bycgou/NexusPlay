-- 轮播图表（新库可并入 biliplus.sql；已有库执行本文件）
USE biliplus;

CREATE TABLE IF NOT EXISTS `banner` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标题',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
  `image_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '图片URL',
  `link_type` tinyint NOT NULL DEFAULT 1 COMMENT '跳转类型：1-视频 2-外链 3-不跳转',
  `video_id` bigint NULL DEFAULT NULL COMMENT '关联视频ID（link_type=1）',
  `link_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '外链（link_type=2）',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序，越小越靠前',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-下线 1-上线',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_status_sort`(`status` ASC, `sort_order` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '首页轮播图表' ROW_FORMAT = Dynamic;

-- 初始数据（可按需修改）
INSERT INTO `banner` (`title`, `description`, `image_url`, `link_type`, `video_id`, `sort_order`, `status`) VALUES
('龙叔经典动作集锦', '回顾成龙电影中的精彩瞬间', '/longshu-banner.jpg', 1, 38, 1, 1),
('城乡爱情故事', '一段跨越城乡的浪漫情缘', '/chengxiang-kiss.jpg', 1, 37, 2, 1),
('孙悟空传奇', '西游记中的经典角色解析', '/sunwukong.jpg', 1, 45, 3, 1);
