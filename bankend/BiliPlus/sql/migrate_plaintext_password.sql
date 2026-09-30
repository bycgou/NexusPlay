-- 批量迁移历史明文密码 123456789 → BCrypt
-- 生成方式：Spring BCryptPasswordEncoder.encode("123456789")，rounds=10
-- 执行前请备份 user / admin_user 表
--
-- 说明：
-- 1. 仅更新 password 恰好等于明文 '123456789' 的行，不会影响已是 BCrypt 的密码
-- 2. 全部旧账号共用同一哈希（因为明文相同）；登录仍用 123456789，校验由 BCrypt.matches 完成
-- 3. password 字段 varchar(100)，BCrypt 固定 60 字符，可容纳

UPDATE `user`
SET `password` = '$2a$10$/K6DcxWNQw/x1looMBOIweuDuyDCLM75Qu2Mbs.tIqhSjzPMS2zii'
WHERE `password` = '123456789';

UPDATE `admin_user`
SET `password` = '$2a$10$/K6DcxWNQw/x1looMBOIweuDuyDCLM75Qu2Mbs.tIqhSjzPMS2zii'
WHERE `password` = '123456789';

-- 迁移后抽查
SELECT COUNT(*) AS still_plain_user FROM `user` WHERE `password` = '123456789';
SELECT COUNT(*) AS still_plain_admin FROM `admin_user` WHERE `password` = '123456789';
SELECT COUNT(*) AS bcrypt_user FROM `user` WHERE `password` LIKE '$2a$%';
