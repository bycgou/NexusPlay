# NexusPlay

B 站风格视频社区（BiliPlus），包含用户前台、后端 API 与管理后台。

## 目录结构

| 目录 | 说明 |
|------|------|
| `frontend/biliPlus` | 用户前台（Vue 3 + Vite + Element Plus） |
| `bankend/BiliPlus` | 后端（Spring Boot 3 + MyBatis + MySQL + Redis） |
| `biliPlusAdmin` | 管理后台（Vue 3 + Vite） |
| `biliplus.sql` | 数据库结构脚本 |
| `FIX_PLAN.md` / `DEV_PLAN.md` / `LIVE_*.md` | 开发与修复计划文档 |

## 快速启动

### 环境

- JDK 17+
- Maven 3.9+
- Node.js 18+
- MySQL 8 / Redis

### 后端

```bash
cd bankend/BiliPlus
# 配置 .env（参考 .env.template）
mvn spring-boot:run
# 默认端口 8081
```

### 用户前台

```bash
cd frontend/biliPlus
npm install
npm run dev
# http://localhost:5173
```

### 管理后台

```bash
cd biliPlusAdmin
npm install
npm run dev
# http://localhost:5174
```

### 数据库

导入根目录 `biliplus.sql`，并按需执行：

- `bankend/BiliPlus/sql/*.sql`（分类/审核/轮播/直播等扩展）

## 主要功能

- 注册登录、视频投稿与播放、弹幕、评论
- 点赞 / 收藏 / 关注
- 私信（WebSocket）
- 搜索视频与用户
- 首页轮播、分类筛选
- 管理端：视频审核、分类、轮播图

## 许可

仅供学习与课程设计使用。
