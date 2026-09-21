# NexusPlay（BiliPlus）

B 站风格的视频社区：投稿与弹幕播放、评论关注、站内通知、直播礼物与 PK、钱包账变、内容举报治理。

项目分三端，同一套后端 API：

| 目录 | 说明 |
|------|------|
| `bankend/BiliPlus` | 后端 API（Spring Boot 3.2 + MyBatis + MySQL + Redis + WebSocket） |
| `frontend/biliPlus` | 用户前台（Vue 3 + Vite 7 + TypeScript + Element Plus） |
| `biliPlusAdmin` | 管理后台（Vue 3 + Vite + Element Plus） |
| `bankend/BiliPlus/sql/biliplus.sql` | 数据库基线，37 张表，含全部新表 |

---

## 功能

### 用户前台

- **账号**：邮箱验证码注册、登录（JWT 双端）、个人资料、修改密码、个人空间
- **视频**：分片投稿、分类频道、搜索、DPlayer 弹幕播放、点赞 / 收藏 / 关注、分享链接与分享计数
- **播放设置**：弹幕开关 / 透明度 / 字号 / 滚动速度 / 显示区域、倍速、默认音量、进度记忆、自动连播；设置页与播放器共用同一份默认值常量，改完即时生效
- **我的稿件**：编辑标题 / 简介 / 分区 / 封面 / 标签，驳回后重新提交，软删除；列表显示审核状态与驳回原因
- **社区**：评论与回复、站内通知（Header 铃铛红点，登录后 30 秒轮询）、播放历史（登录后跨设备续播）、收藏夹、动态 Feed（关注流 / 全站广场）、内容举报
- **直播**：开播与观看、礼物打赏（SVGA 特效）、主播发起 PK、连麦信令（未配置 RTC 时明确降级提示）、直播回放、充值订单与账变明细
- **私信**：WebSocket 实时会话
- **番剧**：番剧方格广场（详情页尚未实现）

### 管理后台

- 视频审核：通过 / 驳回（填原因）/ 下架
- 分类、轮播图、直播房间与分区、礼物、打赏流水
- **钱包账变**：按用户 / 类型 / 业务类型 / 日期区间对账
- **举报处理**：举报成立时联动下架视频或软删评论
- **系统通知**：向全站正常用户广播

> 尚未实现：数据看板、用户管理与封禁、操作日志、番剧详情页、推荐算法增强。

---

## 技术栈

| 层 | 选型 |
|----|------|
| 后端 | JDK 17 · Spring Boot 3.2 · MyBatis · PageHelper · JWT · WebSocket · Redis · spring-dotenv |
| 前台 | Vue 3 · Vite 7 · TypeScript · Pinia · Element Plus · DPlayer · flv.js · SVGA |
| 管理端 | Vue 3 · Vite · Element Plus · vue-tsc |
| 数据 | MySQL 8（InnoDB / utf8mb4） |
| 直播 | SRS 5（Docker，推流 RTMP:1935 / 拉流 HTTP-FLV:8080 / API:1985） |

---

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.9+
- Node.js `^20.19.0 || >=22.12.0`
- MySQL 8、Redis
- Docker（**只有测试直播功能时才需要**，跑 SRS）

### 1. 初始化数据库

```sql
-- 建库
CREATE DATABASE biliplus DEFAULT CHARACTER SET utf8mb4;

-- 导入基线（单文件，含全部 37 张表）
mysql -u root -p biliplus < bankend/BiliPlus/sql/biliplus.sql
```

### 2. 启动后端

```powershell
cd bankend/BiliPlus
Copy-Item .env.template .env     # Windows；Linux/macOS 用 cp .env.template .env
# 按需修改 .env 中的数据库、Redis、JWT 密钥、存储路径
mvn spring-boot:run              # 监听 :8081
```

项目已引入 `spring-dotenv`，`bankend/BiliPlus/.env` 会在启动时自动加载。`.env` 已被 git 忽略，**不要提交真实密钥**。

关键环境变量（完整列表见 `.env.template`）：

| 变量 | 说明 |
|------|------|
| `DB_HOST` / `DB_PORT` / `DB_NAME` / `DB_USERNAME` / `DB_PASSWORD` | 数据库连接 |
| `REDIS_HOST` / `REDIS_PORT` | Redis |
| `JWT_ADMIN_SECRET` / `JWT_PEOPLE_SECRET` | 双端 JWT 密钥，生产必须换成高强度随机串 |
| `VIDEO_UPLOAD_PATH` / `IMAGE_UPLOAD_PATH` | 视频与图片本地存储目录（结尾保留斜杠） |
| `CORS_ALLOWED_ORIGINS` | 跨域来源，逗号分隔 |
| `SRS_RTMP_HOST` / `SRS_RTMP_PORT` / `SRS_FLV_BASE` | SRS 推拉流地址，需与 `.env.development` 里的前端配置一致 |
| `SRS_RTC_ENABLED` | 连麦 RTC 开关，默认 `false`（前端给出降级提示） |
| `LIVE_REPLAY_ENABLED` / `LIVE_REPLAY_URL_TEMPLATE` | 直播回放登记开关与点播地址模板 |

### 3. 启动 SRS（测直播时才需要）

```powershell
docker run -d --name srs --restart unless-stopped `
  -p 1935:1935 -p 1985:1985 -p 8080:8080 -p 8000:8000/udp `
  ossrs/srs:5
```

验证：`http://127.0.0.1:8080/api/v1/versions` 应返回 `{"code":0,...}`。

### 4. 启动用户前台

```powershell
cd frontend/biliPlus
npm install
npm run dev      # http://localhost:5173
```

### 5. 启动管理后台

```powershell
cd biliPlusAdmin
npm install
npm run dev      # http://localhost:5174
```

### 端口一览

| 服务 | 端口 | 说明 |
|------|------|------|
| 用户前台 Vite | 5173 | 代理 `/api`、`/ws`、`/images`、`/video-files`、`/srs-live` |
| 管理后台 Vite | 5174 | 同上 |
| 后端 API | 8081 | |
| MySQL | 3306 | |
| Redis | 6379 | |
| SRS RTMP | 1935 | OBS 推流 |
| SRS HTTP-FLV | 8080 | 观众拉流 |
| SRS API | 1985 | 推流状态查询 |

---

## 构建与测试

```powershell
# 后端
cd bankend/BiliPlus
mvn -DskipTests compile      # 仅编译
mvn test                     # 单元测试（Mockito，不依赖 MySQL/Redis）

# 用户前台
cd frontend/biliPlus
npm run build                # vite build

# 管理后台
cd biliPlusAdmin
npm run build                # vue-tsc -b && vite build
```

---

## 局域网联调

双方在同一局域网，且对方能 ping 通你的 IP。

1. 按上面顺序启动后端、前台、管理端，测直播再加 SRS。
2. 对方在浏览器打开 `http://<你的局域网IP>:5173`。
3. 你用 OBS 推流：服务器 `rtmp://localhost:1935/live`，串流密钥以开播后主播台显示为准。

几个要点：

- **流量走 Vite 代理**。前台已把 `/api`、`/ws`、`/images`、`/video-files`、`/srs-live` 都代理到本机后端与 SRS，所以朋友只需要能访问你的 5173 端口。
- **Docker 的 SRS 端口通常只对本机可见**（Windows + WSL2 下实测局域网 IP 连不上 1935/8080），这正是拉流必须走 `/srs-live` 代理的原因。
- **媒体地址是相对路径**（`/images/...`、`/video-files/...`），由当前站点同源解析。若历史数据里还是 `http://localhost:8081/...`，需要改成相对路径，否则在别人机器上会裂图。
- **朋友不能当主播**：除非把 SRS 的 1935 端口真正暴露到局域网，否则他的 OBS 推不进来。

---

## 项目结构

```
bankend/BiliPlus/src/main/java/com/biliplus/
  controller/     user/  admin/  pulic/     三端接口
  service/Impl/                            业务实现（单测在 src/test/java）
  mapper/ + resources/mapper/              MyBatis 接口与 XML
  websocket/                               /ws/chat 私信、/ws/live 直播
  constant/                                视频状态、账变类型、通知类型等枚举
  interceptor/                             JWT 用户端 / 管理端鉴权
  config/                                  CORS、静态资源、WebSocket

frontend/biliPlus/src/
  api/              接口封装
  composables/      useLocalSettings、useLiveSocket、useVideoProgress…
  constants/        播放设置默认值（设置页与播放器共用）
  views/            home/ live/ setting/ notify/ dynamic/ anime/ chat/…
  router/           路由与登录守卫

biliPlusAdmin/src/
  api/  views/  router/                     审核、直播、礼物、账变、举报、通知

bankend/BiliPlus/sql/biliplus.sql           数据库基线（37 表）
bankend/BiliPlus/.env.template              后端环境变量模板
frontend/biliPlus/.env.development          前台开发环境变量
```

---

## API 约定

- 用户端前缀 `/pp/**`，管理端前缀 `/admin/**`
- 统一响应：`{ code: 1 成功 / 0 失败, msg, data }`
- 鉴权：请求头 `Authorization: Bearer <token>`；未登录访问个人接口返回 401，前端跳转登录页
- 公开 GET（视频、评论、分类、轮播、直播广场、番剧、全站动态等）不需要登录

---

## 说明

- 仅供学习与课程设计使用。
- 数据库口令、JWT 密钥、邮箱授权码一律通过 `.env` 注入，仓库里只保留 `.env.template`。
- 直播、充值目前是模拟支付与本地 SRS 链路，未对接真实支付网关与 CDN。
