-- 视频审核不通过原因字段（已有库请执行；新库已在 biliplus.sql 中）
USE biliplus;

ALTER TABLE `video`
    ADD COLUMN IF NOT EXISTS `reject_reason` varchar(500)
        CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci
        NULL DEFAULT NULL COMMENT '审核不通过原因'
        AFTER `share_count`;

-- 状态说明：0待审 1正常 2下架 3审核不通过
