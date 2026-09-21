-- ============================================================
-- 局域网/多域名部署修复：把入库的绝对媒体地址改成相对路径
--
-- 背景：UploadController 此前用 app.external-url（默认 http://localhost:8081）
-- 拼接 video_url / cover_url 等字段并落库。局域网访问者浏览器解析到的是
-- 「他自己的机器」，导致封面裂图、视频无法播放。
--
-- 现已改为只存 access-prefix + 文件名（如 /video-files/xxx.mp4），
-- 由前端站点同源解析，再经 Vite（dev）或 Nginx（生产）转发到后端。
--
-- 本脚本用于清洗存量数据。可重复执行（幂等）。
-- ============================================================

SET NAMES utf8mb4;

-- 视频：播放地址与封面
UPDATE `video`
   SET `video_url` = REPLACE(`video_url`, 'http://localhost:8081', '')
 WHERE `video_url` LIKE 'http://localhost:8081%';

UPDATE `video`
   SET `cover_url` = REPLACE(`cover_url`, 'http://localhost:8081', '')
 WHERE `cover_url` LIKE 'http://localhost:8081%';

-- 首页轮播图
UPDATE `banner`
   SET `image_url` = REPLACE(`image_url`, 'http://localhost:8081', '')
 WHERE `image_url` LIKE 'http://localhost:8081%';

-- 轮播外链（link_type=2）也可能是绝对地址
UPDATE `banner`
   SET `link_url` = REPLACE(`link_url`, 'http://localhost:8081', '')
 WHERE `link_url` LIKE 'http://localhost:8081%';

-- 番剧封面
UPDATE `anime`
   SET `cover_url` = REPLACE(`cover_url`, 'http://localhost:8081', '')
 WHERE `cover_url` LIKE 'http://localhost:8081%';

-- 番剧剧集播放地址
UPDATE `anime_episode`
   SET `video_url` = REPLACE(`video_url`, 'http://localhost:8081', '')
 WHERE `video_url` LIKE 'http://localhost:8081%';

-- 直播间封面
UPDATE `live_room`
   SET `cover_url` = REPLACE(`cover_url`, 'http://localhost:8081', '')
 WHERE `cover_url` LIKE 'http://localhost:8081%';

-- 礼物图标
UPDATE `gift`
   SET `icon_url` = REPLACE(`icon_url`, 'http://localhost:8081', '')
 WHERE `icon_url` LIKE 'http://localhost:8081%';

-- 分类图标
UPDATE `category`
   SET `icon` = REPLACE(`icon`, 'http://localhost:8081', '')
 WHERE `icon` LIKE 'http://localhost:8081%';

-- 直播回放点播地址
UPDATE `live_replay`
   SET `play_url` = REPLACE(`play_url`, 'http://localhost:8081', '')
 WHERE `play_url` LIKE 'http://localhost:8081%';

-- 用户头像
UPDATE `user`
   SET `avatar` = REPLACE(`avatar`, 'http://localhost:8081', '')
 WHERE `avatar` LIKE 'http://localhost:8081%';

-- ------------------------------------------------------------
-- 校验：下面两条查询结果都应为空
-- ------------------------------------------------------------
-- SELECT id, video_url FROM video    WHERE video_url LIKE 'http://%';
-- SELECT id, cover_url FROM video    WHERE cover_url LIKE 'http://%';
