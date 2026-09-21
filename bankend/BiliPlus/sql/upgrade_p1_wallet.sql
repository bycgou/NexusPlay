-- ============================================================
-- P1 增量脚本：钱包账变与充值订单
--
-- 说明：
-- 1. 与 biliplus.sql 基线保持同一套定义，基线已同步追加这两张表。
-- 2. wallet_transaction.balance_after 表示「变动后余额」，余额口径由 biz_type 决定：
--    - recharge / gift / adjust → 对应 user_wallet.balance
--    - host_income               → 对应 host_income.total_income（主播累计收益）
-- ============================================================

SET NAMES utf8mb4;

DROP TABLE IF EXISTS `wallet_transaction`;
CREATE TABLE `wallet_transaction` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `type` tinyint NOT NULL COMMENT '1充值 2送礼支出 3主播收入 4系统调整',
  `amount` bigint NOT NULL COMMENT '变动金额，正为入账，负为出账',
  `balance_after` bigint NOT NULL COMMENT '变动后余额，口径见 biz_type',
  `biz_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'recharge|gift|host_income|adjust',
  `biz_id` bigint NULL DEFAULT NULL COMMENT '关联业务ID：充值订单ID / 打赏流水ID',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_time`(`user_id` ASC, `create_time` ASC) USING BTREE,
  INDEX `idx_biz`(`biz_type` ASC, `biz_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '钱包账变流水' ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `recharge_order`;
CREATE TABLE `recharge_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `order_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '业务订单号',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `amount` bigint NOT NULL COMMENT '硬币数量',
  `pay_amount` decimal(10, 2) NOT NULL COMMENT '应付金额',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '0待支付 1已支付 2已取消',
  `pay_channel` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'mock|alipay|wechat',
  `paid_time` datetime NULL DEFAULT NULL COMMENT '支付时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_order_no`(`order_no` ASC) USING BTREE,
  INDEX `idx_user_status`(`user_id` ASC, `status` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '充值订单' ROW_FORMAT = Dynamic;
