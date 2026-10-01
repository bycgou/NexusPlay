-- 直播礼物目录种子数据
-- 名称必须与前端 GiftEffectLayer 的 SVGA_MAP 一致，否则特效能不出画面
-- 图标使用 /gifts/effects/*.png（随前端构建一起部署）

INSERT INTO gift (name, icon_url, price, effect_level, sort_order, status) VALUES
  ('点赞',       '/gifts/effects/666.png',       1,   1, 1,  1),
  ('紫色玫瑰',   '/gifts/effects/紫色玫瑰.png',  5,   1, 2,  1),
  ('爱心气球',   '/gifts/effects/爱心气球.png',  10,  1, 3,  1),
  ('爱的漂流瓶', '/gifts/effects/爱的漂流瓶.png',30,  1, 4,  1),
  ('钻石',       '/gifts/effects/钻石.png',      20,  1, 5,  1),
  ('爱心熊熊',   '/gifts/effects/爱心熊熊.png',  50,  2, 6,  1),
  ('红色跑车',   '/gifts/effects/红色跑车.png',  100, 2, 7,  1),
  ('爱心直升机', '/gifts/effects/爱心直升机.png',150, 2, 8,  1),
  ('幸福马车',   '/gifts/effects/幸福马车.png',  200, 2, 9,  1),
  ('LV包',       '/gifts/effects/LV包.png',      200, 2, 10, 1),
  ('超级火箭',   '/gifts/rocket.svg',            500, 3, 11, 1),
  ('紫色城堡',   '/gifts/effects/紫色城堡.png',  300, 3, 12, 1),
  ('天马',       '/gifts/effects/天马.png',      500, 3, 13, 1),
  ('财神到',     '/gifts/effects/财神到.png',    800, 3, 14, 1),
  ('666',        '/gifts/effects/666.png',       1,   1, 15, 1);
