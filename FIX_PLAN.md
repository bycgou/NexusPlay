# BiliPlus / NexusPlay 功能修复计划

基于前后端代码审计，按优先级落地修复。  
范围：`bankend/BiliPlus` + `frontend/biliPlus`。  
日期：2026-09-13

---

## P0 — 主流程必须可用

| # | 问题 | 修复方案 | 涉及文件 |
|---|------|----------|----------|
| P0-1 | `VITE_BASE_API` 与 `.env` 变量名不一致 | 统一为 `VITE_BASE_API=/api` | `frontend/.env.development` |
| P0-2 | 未登录看首页/详情/评论 401 | 拦截器放行 `GET /pp/videos/**`、`GET /pp/comments`；弹幕仅 GET 放行 | `WebMvcConfiguration` |
| P0-3 | 评论 userId 写死为 3 | 从 `UserContext` 取当前用户 | `CommentServiceImpl`、`AllConstant` |
| P0-4 | 删除评论空实现 | 实现软删 + 本人/校验 | `CommentMapper`、`CommentServiceImpl`、XML |
| P0-5 | 评论列表无用户昵称/头像 | 查询联表 `user` | `CommentMapper.xml`、`Comment` 实体或 VO |
| P0-6 | 私聊创建字段名错误 | 前端改为 `targetUserId` | `ChatView.vue` |
| P0-7 | 弹幕 type 映射错误 | 前端改为 top=2,bottom=3,right=1（字符串保持后端解析） | `DanmakuPlayer.vue` |
| P0-8 | 弹幕 userId 信任客户端 | 后端从 JWT 取 userId | `DanmakuServiceImpl` |
| P0-9 | 视频列表不过滤 status | 查询 `status=1` | `VideoMapper`/`VideoMapper.xml` |
| P0-10 | 投稿不写 status | 上传时 `status=1`、计数默认 0 | `PeopleUserServiceImpl` |

---

## P1 — 越权与数据一致性

| # | 问题 | 修复方案 | 涉及文件 |
|---|------|----------|----------|
| P1-1 | 聊天不校验会话成员 | saveMessage/getMessage/markAsRead 调用 `isUserInConversation` | `ChatServiceImpl`、`ChatController` |
| P1-2 | 投稿/改资料 IDOR | 强制使用 `UserContext` 的 userId | `PeopleUserController`、`PeopleUserServiceImpl` |
| P1-3 | 点赞/评论不回写 video 计数 | 同步 `like_count`/`comment_count` | `InteractionServiceImpl`、`CommentServiceImpl`、`VideoMapper` |
| P1-4 | CORS 配置无效 | 增加 `WebMvcConfigurer.addCorsMappings` | `WebMvcConfiguration` |
| P1-5 | 上传接口全公开 | chunk/merge/cover 需要登录 | `WebMvcConfiguration` |
| P1-6 | `resetUnreadCount`/`selectById` 缺 SQL | 补全 XML | 两个 Mapper XML |
| P1-7 | `findPrivateBetween` 不过滤 leave_time | 加 `leave_time IS NULL` | `ChatConversationMapper.xml` |
| P1-8 | WS 消息 ID 假数据 | 用 `msg.getId()`，并 echo 发送方 | `ChatWebSocketHandler` |
| P1-9 | WS userId 强转 Integer | 改用 Number.longValue() | `JwtHandshakeInterceptor` |
| P1-10 | 上传临时目录删除失败 | 递归删除 | `UploadController` |
| P1-11 | 关注状态前端不加载 | 调用 `getUserInteractionStatus` | `VideoDetail.vue` |

---

## P2 — 体验与半成品（本次只做低风险项）

| # | 问题 | 处理 |
|---|------|------|
| P2-1 | 评论区模板多余 `</div>` | 修模板 |
| P2-2 | `ChatView` `isMobile` 未定义 | 补 computed |
| P2-3 | request timeout 5s | 改为 30s |
| P2-4 | 401 跳首页 | 跳 `/login` |
| P2-5 | 弹幕/评论发前未登录校验 | 前端提示登录 |

不在本次范围（另开任务）：分类 CRUD、管理端注册、视频审核流、搜索、番剧/直播、设置页子路由、播放量精确统计策略、生产 CORS 白名单细化。

---

## 验收标准

1. `mvn -DskipTests compile` 后端通过。
2. 未登录可浏览首页视频列表、视频详情、评论、弹幕列表。
3. 登录用户评论显示自己的昵称头像，userId 为真实登录用户。
4. 删除自己评论成功，他人评论不可删。
5. 私聊可成功创建会话。
6. 弹幕顶部/底部/滚动类型正确往返。
7. 视频列表只出现 status=1 的视频。
8. 点赞后 video.like_count 与互动接口 likeCount 一致。
