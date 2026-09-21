# NexusPlay 下一阶段升级开发文档

> 范围：体验收口 · 直播闭环 · 资金流水 · 社区功能 · 管理运营  
> 仓库：`bankend/BiliPlus` · `frontend/biliPlus` · `biliPlusAdmin`  
> 日期：2026-09-13  
> 前置：`FIX_PLAN.md` · `DEV_PLAN.md` · `LIVE_PLAN.md` · `LIVE_FULL_PLAN.md`  
> 性质：在现有 MVP 之上**增量升级**，不推翻已实现模块

---

## 1. 背景与目标

### 1.1 背景

NexusPlay（BiliPlus）已具备 B 站风格视频社区主链路：

| 已有能力 | 状态 |
|----------|------|
| 注册登录（JWT 双端）、投稿分片上传、审核 | 可用 |
| 视频播放、弹幕、评论、赞/藏/关、搜索 | 可用 |
| 私信 WebSocket、分类、轮播、用户空间 | 可用 |
| 直播开播 / 拉流 / 礼物扣款 / PK / 管理端监管 | 基本可用 |
| 管理端：审核、分类、轮播、直播、礼物、流水 | 已接 API |

但存在明显产品债与工程债（详见 `UPGRADE_PLAN` 盘点结论）：

1. **半成品**：播放设置不生效、连麦无媒体流、PK 缺主播发起、通知空壳、分享/稿件管理/番剧详情未完成  
2. **资金不合规**：充值为测试直充，无账变流水，无法对账  
3. **社区闭环缺失**：无播放历史、通知、动态、举报治理  
4. **运营能力弱**：管理端无用户管理、数据看板、RBAC  
5. **环境债**：WS/OBS/CORS/存储路径硬编码，生产密钥默认值入库  

### 1.2 本期目标

| 模块 | 目标 |
|------|------|
| 体验收口 | 设置真正生效；配置外置；稿件可编辑；下线 test 路由 |
| 直播闭环 | 主播可发起 PK；连麦音视频可用（或明确降级）；回放入库 |
| 资金合规 | 充值/送礼写账变；管理端可对账 |
| 社区功能 | 通知中心、播放历史、动态 Feed、举报治理 |
| 运营后台 | 用户管理、数据看板、操作日志 |
| 工程化 | env 配置、密钥外置、存储路径配置化 |

### 1.3 明确不做（本期）

- 真实支付网关对接（支付宝/微信），仅预留订单结构与模拟支付  
- CDN、多码率转码集群（预留 FFmpeg 任务接口即可）  
- 专栏、充电、合集、课堂等扩展业态  
- ES 搜索集群（数据量上来后再做）  
- 多人连麦（>2）、多人 PK  

### 1.4 成功标准（整体）

1. 用户改播放设置后，刷新/换视频仍生效（弹幕、倍速、进度记忆）  
2. 双主播可发起并完成 PK，双方观众见分屏与比分  
3. 连麦同意后双方可见画面声音；RTC 不可用时 UI 明确提示  
4. 充值与送礼均产生 `wallet_transaction`，管理端流水可对账  
5. 用户能收到评论/审核/关注类通知；个人空间可见播放历史  
6. 评论/视频/直播可被举报，管理端可处理  
7. 管理端有基础数据看板与用户封禁能力  
8. 核心 Service 单测通过；`mvn test` 绿；前端 `npm run build` 通过  

---

## 2. 现状盘点（与本计划相关）

### 2.1 已有可复用资产

| 资产 | 位置 | 升级时如何复用 |
|------|------|----------------|
| 用户 JWT + ThreadLocal | `JwtTokenPeopleInterceptor` / `UserContext` | 全部新写接口鉴权 |
| 管理端 JWT | `JwtTokenAdminInterceptor` | 管理端新模块 |
| 直播房间/礼物/PK/连麦 | `LiveController` + Service + WS | 补发起 PK、连麦媒体、回放 |
| 钱包扣款 | `GiftServiceImpl` + `UserWalletMapper.deduct` | 扩展为账变流水 |
| 本地偏好同步 | `useLocalSettings.ts` + `user_settings` | 播放器接入消费端 |
| 私信 WS | `/ws/chat` + `ChatWebSocketHandler` | 通知可复用推送通道或独立 WS |
| 视频弹幕播放器 | `DanmakuPlayer.vue`（DPlayer） | 番剧选集、历史续播 |
| 管理端壳 | `biliPlusAdmin` 侧边栏布局 | 加用户/通知/举报/看板菜单 |
| 礼物 SVGA 特效 | `GiftEffectLayer.vue` + `public/gifts/effects/` | 直播继续使用 |
| 单测习惯 | `src/test/java/com/biliplus/service/*Test.java` | 每阶段配套 Service 单测 |

### 2.2 缺口清单（本计划要补的）

| 缺口 | 类型 | 归属阶段 |
|------|------|----------|
| `DanmakuPlayer` 不读播放设置 | 产品债 | P0 |
| WS/OBS/CORS/存储硬编码 | 工程债 | P0 |
| 稿件无编辑/删除/重投 | 产品债 | P0 |
| 分享、通知空壳 | 产品债 | P0 / P2 |
| `/test`、`/testTwo` 生产路由 | 工程债 | P0 |
| 连麦无 WebRTC 挂流 | 半成品 | P1 |
| PK 主播侧无发起入口 | 半成品 | P1 |
| 无 `wallet_transaction` / 充值订单 | 合规债 | P1 |
| 无直播回放 | 功能缺 | P1 |
| 无通知/历史/动态/举报 | 社区缺 | P2 |
| 管理端无用户/看板/权限 | 运营缺 | P3 |
| 推荐算法过弱、番剧详情缺 | 体验缺 | P2 / P3 |

### 2.3 数据层现状

主库 `biliplus.sql` 已有 28 表，直播礼物链路（`gift` / `user_wallet` / `gift_record` / `host_income` / `live_pk` / `live_mic_session`）齐全。

**完全缺失**（本计划新增）：

```text
play_history          播放历史
notification          站内通知
report                举报
favorite_folder       收藏夹
dynamic / dynamic_like 动态
wallet_transaction    钱包账变
recharge_order        充值订单
live_replay           直播回放
admin_operation_log   管理端操作日志
admin_role / admin_role_rel  （可选，P3 简化为 admin_user.role 字段）
```

---

## 3. 技术架构（增量）

### 3.1 总览

```text
┌─────────────────┐     HTTP/WS      ┌──────────────────────┐
│ frontend        │ ◄──────────────► │ Spring Boot :8081    │
│ biliPlus        │                  │  /pp/**  /ws/**      │
└─────────────────┘                  │  MyBatis + Redis     │
┌─────────────────┐                  │  JWT (user/admin)    │
│ biliPlusAdmin   │ ◄──────────────► │                      │
└─────────────────┘                  └──────────┬───────────┘
                                                │
                     ┌──────────────────────────┼──────────────────┐
                     ▼                          ▼                  ▼
                 MySQL 8                    Redis              SRS 5.x
              biliplus 库                  配置/计数/           RTMP/FLV
              + 本计划新表                  通知未读              + RTC(可选)
```

### 3.2 环境配置约定（P0 强制）

**前端** `frontend/biliPlus/.env.development` / `.env.production`：

```bash
VITE_BASE_API=/api
VITE_WS_CHAT=ws://localhost:8081/ws/chat
VITE_WS_LIVE=ws://localhost:8081/ws/live
VITE_OBS_RTMP=rtmp://localhost:1935/live
VITE_SRS_FLV_BASE=http://localhost:8080/live
```

**后端** `application.yml` 敏感项全部 `${ENV}` 注入：

```yaml
biliplus:
  jwt:
    admin-secret-key: ${JWT_ADMIN_SECRET}
    people-secret-key: ${JWT_PEOPLE_SECRET}
video:
  upload:
    base-path: ${VIDEO_UPLOAD_PATH:D:/MyOSS/biliPlus/video-website/Videos/}
image:
  upload:
    base-path: ${IMAGE_UPLOAD_PATH:D:/MyOSS/biliPlus/video-website/Images/}
live:
  srs:
    rtmp-host: ${SRS_RTMP_HOST:localhost}
    http-flv-base: ${SRS_FLV_BASE:http://localhost:8080/live}
    rtc-enabled: ${SRS_RTC_ENABLED:false}
app:
  cors:
    allowed-origins: ${CORS_ALLOWED_ORIGINS:http://localhost:5173,http://localhost:5174}
```

前端禁止再写死 `localhost:8081` / `rtmp://localhost`，一律读 env。

### 3.3 统一响应与错误

- 继续使用 `Result` / `PageResult`  
- 业务异常：`BusinessException` + `GlobalExceptionHandler`  
- 401：前端跳 `/login`；403：统一 `Result.error("无权限")`  
- 新接口未登录：与现有一致，由 `JwtTokenPeopleInterceptor` 拦截  

---

## 4. 阶段划分总览

| 阶段 | 名称 | 粗估 | 核心交付 |
|------|------|------|----------|
| **P0** | 体验收口与工程债 | 3–4 天 | 设置生效、配置外置、稿件管理、分享、清 test |
| **P1** | 直播闭环与资金流水 | 4–5 天 | PK 发起、连麦媒体、账变订单、回放 |
| **P2** | 社区闭环 | 4–5 天 | 通知、历史、收藏夹、动态、举报 |
| **P3** | 运营与体验增强 | 3–4 天 | 看板、用户管理、操作日志、番剧详情、推荐 |
| **合计** | — | **约 14–18 人日** | 两人可前后端并行压缩至 ~10 人日 |

执行约定（与 `DEV_PLAN.md` 一致）：

1. 每阶段：编码 → 单测 → 自测清单 → 合并 → 下一阶段  
2. 不引入与阶段无关的大重构  
3. 表结构以 `biliplus.sql` 为基线，增量脚本放 `bankend/BiliPlus/sql/upgrade_*.sql`  
4. 前后端字段契约先写在本文档接口表，实现时不得随意改名  

---

## 5. P0 — 体验收口与工程债

### 5.1 播放设置真正生效

**需求**

- 用户在 `/setting/player` 修改的弹幕开关、字号、透明度、不透明度区域、播放速度、自动连播、进度记忆，必须被视频详情播放器消费  
- 未登录读 localStorage；登录后与 `user_settings` 同步（已有 `useLocalSettings`）  

**涉及文件**

| 端 | 文件 | 改动 |
|----|------|------|
| 前台 | `views/home/Main/Video/components/DanmakuPlayer.vue` | 启动时读 `useLocalSettings('player', DEFAULTS)`，初始化 DPlayer 弹幕/倍速；`ended` 时按 `autoNext` 拉推荐；`timeupdate` 节流写 progress |
| 前台 | `views/setting/components/PlayerSettings.vue` | 字段与 defaults 对齐，避免键名不一致 |
| 后端 | `UserSettingsController` / `user_settings` | 已有，无需改结构；确认 JSON 键稳定 |

**建议 defaults（前后端共用语义）**

```ts
{
  danmakuEnabled: true,
  danmakuOpacity: 0.8,
  danmakuFontSize: 25,
  danmakuSpeed: 1,          // DPlayer 透传
  danmakuArea: 0.5,         // 顶部占比 0~1
  playbackRate: 1,
  autoNext: true,
  rememberProgress: true,
  volume: 0.7
}
```

**验收**

1. 设置页改弹幕透明度 → 打开任一视频，弹幕变透明  
2. 设置 1.5 倍速 → 播放器为 1.5x  
3. 关闭「进度记忆」→ 刷新不续播；开启后刷新从上次位置续播  
4. 开启自动连播 → 播完自动进推荐第一条（或同 UP 主下一条）  

**测试**

- 前端：dev 冒烟上述 4 条；`npm run build` 通过  
- 后端：现有 `user_settings` 读写不回归  

---

### 5.2 环境配置外置

**前端**

| 位置 | 现状 | 改为 |
|------|------|------|
| `composables/useLiveSocket.ts` | `ws://localhost:8081/ws/live` | `import.meta.env.VITE_WS_LIVE` |
| `composables/useChatSocket.ts` | 同类硬编码 | `VITE_WS_CHAT` |
| `views/live/LiveStart.vue` | `obsServer()` 写死 rtmp | `import.meta.env.VITE_OBS_RTMP` |
| `vite.config.ts` 代理 | 保留开发代理，生产用 env | 文档说明 |

**后端**

- `WebMvcConfiguration.addCorsMappings`：读取 `app.cors.allowed-origins` 列表，禁止写死仅 localhost  
- JWT secret、DB 账号、上传路径：全部 `${ENV}`，`.env.template` 补全说明  
- `application-prod.yml`：强制覆盖生产域名 CORS、关闭 debug  

**验收**

1. 前端源码 grep 无 `localhost:8081` / `rtmp://localhost` 字面量（vite 代理注释除外）  
2. 修改 `.env` 中 WS 地址后，直播弹幕连到新地址  
3. 后端配置非法 CORS 时启动失败或日志警告，生产配置生效  

---

### 5.3 稿件管理闭环

**需求**

用户在「设置 → 稿件」对自有视频：查看状态 → 编辑信息 → 删除/下架 → 驳回后修改重提。

**后端接口**（挂 `PeopleUserController` 或独立 `MyVideoController`，路径统一 `/pp/people/my/videos`）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/pp/people/my/videos` | 已有，返回 status/rejectReason |
| PUT | `/pp/people/my/videos/{id}` | 仅作者；改 title/desc/categoryId/coverUrl/tags；`status=0` 重新进入待审（若原被驳回/已下架） |
| DELETE | `/pp/people/my/videos/{id}` | 软删 `status=-1` 或 `deleted=1`；列表不再出现 |
| POST | `/pp/people/my/videos/{id}/resubmit` | 驳回稿件修改后重提：`status=0`，清空 rejectReason |

**约束**

- 必须 `UserContext.getCurrentUserId() == video.userId`，否则 403  
- 已通过（status=1）的稿件：编辑标题/简介可直接生效；改视频源文件本期不支持（需重新投稿）  
- 删除后详情页 404  

**前端**

- `views/setting/components/VideoSettings.vue`：每条稿件操作按钮（编辑 / 重提 / 删除）  
- 编辑弹窗：标题、简介、分类、封面（复用 `ImageUploader`）、标签  

**验收**

1. 非作者调 PUT/DELETE → 403  
2. 驳回稿件编辑后重提 → 管理端待审列表出现  
3. 删除后前台列表/详情不可见  

**测试**

- `PeopleUserServiceImplTest`：编辑鉴权、重提状态机、软删  

---

### 5.4 分享

**需求**

- 视频详情「分享」：复制链接 `origin/video/:id?from=share`；支持 `navigator.share`（移动端）  
- 可选：接口记录 `share_count`（`video` 表加字段或 Redis 计数）  

**前端**

- `VideoDetail.vue`：替换 `ElMessage.info('分享功能暂未实现')`  
- 复制成功 toast「链接已复制」  

**后端（可选增强）**

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/pp/videos/{id}/share` | 登录可选；Redis INCR + 回写 video.shareCount |

**验收**

1. 点击分享 → 剪贴板有完整 URL  
2. 未实现 share API 时前端不报错  

---

### 5.5 清理半成品路由与组件

| 动作 | 目标 |
|------|------|
| 路由删除 | `/test`、`/testTwo`（`router/index.ts`） |
| 目录隔离 | `views/test/**`、`views/components/test/**` 移入 `_legacy/` 或删除 |
| Header 链接 | 确认无指向 test 的入口 |
| 推荐作者 | `RecommendSidebar` 等处 `用户{id}` 改为接口返回真实 nickname |

**验收**

1. 访问 `/test` 进 404  
2. 生产 build 无对 test 组件的引用告警  

---

### 5.6 P0 自测清单

- [ ] 播放设置四条冒烟通过  
- [ ] env 外置后直播 WS / OBS 文案正确  
- [ ] 稿件编辑/重提/删除全流程  
- [ ] 分享复制成功  
- [ ] `/test` 404  
- [ ] `mvn -DskipTests compile` 通过  
- [ ] 前台 `npm run build` 通过  
- [ ] 相关 Service 单测绿  

---

## 6. P1 — 直播闭环与资金流水

### 6.1 主播发起 PK

**现状**：后端 `POST /pp/live/pk/invite` 已有；观众侧可响应；主播台/观看页缺主动发起 UI。

**前端**

| 页面 | 改动 |
|------|------|
| `LiveStart.vue` | 「发起 PK」→ 拉 `GET /pp/live/rooms`（排除自己）→ 选择对手 → `pkInvite({ opponentRoomId })` |
| `LiveRoom.vue`（主播模式） | 同上；监听 `pk_invite` 弹窗同意/拒绝 → `respondPk` |
| `useLiveSocket` | 已有 `onPkInvite` / `onPkCancel`，补 UI 绑定 |

**后端（补强）**

| 项 | 说明 |
|----|------|
| invite 校验 | 双方 `status=1`、不在其他 PK 中、不能邀请自己房间 |
| 自动结束 | `duration_sec` 到期由定时任务或前端倒计时触发 `POST /pp/live/pk/{id}/end` |
| WS | 服务端广播 `pk_start` / `pk_score` / `pk_end` / `pk_invite` / `pk_cancel`（Handler 已有则回归） |

**验收**

1. A 主播邀请 B → B 收到弹窗 → 同意 → 双方观众见分屏 + 比分条  
2. PK 中送礼 → 双方 `pk_score` 变化  
3. 到期或手动结束 → 恢复单屏，`live_pk.status=2`  

**测试**

- `LivePkServiceImplTest`：重复邀请、拒绝、比分累加、结束鉴权  

---

### 6.2 连麦音视频（1v1）

**现状**：信令与表结构已有；`rtc-enabled: false`；`LiveRoom.vue` 的 `guestVideoEl` 无挂流。

**选型（本期二选一，默认 A）**

| 方案 | 说明 | 适用 |
|------|------|------|
| **A. SRS RTC** | `live.srs.rtc-enabled=true`，信令 `mic_ready` 返回 rtc 推拉地址 | 本地 Docker 可验 |
| **B. 降级提示** | 未配置 RTC 时，`mic_ready` 返回 `rtcAvailable=false`，前端 toast「当前环境暂不支持连麦」 | 无 RTC 时保底 |

**信令（沿用 LIVE_FULL_PLAN，客户端 → 服务端）**

```json
{ "type": "mic_apply", "roomId": 1 }
{ "type": "mic_accept", "sessionId": 9, "agree": true }
{ "type": "mic_leave" }
```

服务端 → 客户端：

```json
{ "type": "mic_apply", "roomId": 1, "userId": 9, "nickname": "观众X", "sessionId": 9 }
{ "type": "mic_ready", "role": "host|guest", "rtcRoom": "live-1", "token": "...", "rtcAvailable": true }
{ "type": "mic_end", "roomId": 1 }
```

**后端**

- `LiveMicService.accept`：配置了 RTC 时签发短时 token；未配置则 `rtcAvailable=false` 但仍改 status  
- 下播 `stopLive`：级联结束进行中 mic（status=2）并广播 `mic_end`  
- 拦截：仅主播可 accept；仅本人可 leave  

**前端**

- 观众：申请连麦 → `mic_ready` 且 `rtcAvailable` → 拉/推 RTC 到 `guestVideoEl`  
- 主播：`LiveStart` 申请列表同意 → 本地预览区出现连麦小窗  
- 结束/下播：销毁 RTC，恢复布局  

**验收**

1. RTC 开启：同意后双方可见画面（可先只验证信令 + token 返回）  
2. RTC 关闭：同意后提示「环境不支持」，不出现假小窗  
3. 下播强制结束连麦，`live_mic_session.status=2`  

**测试**

- `LiveMicServiceImplTest`：申请重复、非主播 accept、leave 状态  

---

### 6.3 钱包账变与充值订单

**问题**：`GiftServiceImpl.recharge` 直接 `UPDATE user_wallet`，无流水。

**新表**

```sql
-- bankend/BiliPlus/sql/upgrade_p1_wallet.sql

CREATE TABLE IF NOT EXISTS `wallet_transaction` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `type` TINYINT NOT NULL COMMENT '1充值 2送礼支出 3主播收入 4系统调整',
  `amount` BIGINT NOT NULL COMMENT '变动金额，正为入账负为出账',
  `balance_after` BIGINT NOT NULL,
  `biz_type` VARCHAR(32) NOT NULL COMMENT 'recharge|gift|host_income|adjust',
  `biz_id` BIGINT NULL COMMENT '关联订单/礼物记录ID',
  `remark` VARCHAR(255) NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY `idx_user_time` (`user_id`, `create_time`),
  KEY `idx_biz` (`biz_type`, `biz_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='钱包账变流水';

CREATE TABLE IF NOT EXISTS `recharge_order` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `order_no` VARCHAR(64) NOT NULL COMMENT '业务订单号',
  `user_id` BIGINT NOT NULL,
  `amount` BIGINT NOT NULL COMMENT '硬币数量',
  `pay_amount` DECIMAL(10,2) NOT NULL COMMENT '应付金额（模拟）',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0待支付 1已支付 2已取消 3已退款',
  `pay_channel` VARCHAR(32) NULL COMMENT 'mock|alipay|wechat',
  `paid_time` DATETIME NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_status` (`user_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='充值订单';
```

**接口**

| 方法 | 路径 | 鉴权 | 说明 |
|------|------|------|------|
| POST | `/pp/live/wallet/recharge/orders` | 登录 | 创建订单 body:`{amount}` → 返回 `{orderNo, payAmount}` |
| POST | `/pp/live/wallet/recharge/orders/{orderNo}/pay` | 登录 | **模拟支付**：status=1，加余额，写账变 |
| POST | `/pp/live/wallet/recharge` | 登录 | **兼容旧接口**：内部改为创建+立即支付（仅 dev）或标记 deprecated |
| GET | `/pp/live/wallet/transactions` | 登录 | 分页查自己账变 |
| GET | `/admin/live/wallet/transactions` | 管理端 | 按 userId/时间/type 筛选 |

**送礼账变（改 `GiftServiceImpl.sendGift`）**

在同一事务中：

1. `deduct` 扣余额  
2. INSERT `gift_record`（已有）  
3. INSERT `wallet_transaction`：`type=2, amount=-total, biz_type=gift, biz_id=record.id`  
4. `host_income` 增加（已有）+ INSERT `type=3` 主机账变（可选，以 host_user_id 记）  
5. PK 加分、WS 广播（已有）  

**验收**

1. 创建订单 → 模拟支付 → 余额增加，`recharge_order.status=1`，有 type=1 流水  
2. 送礼后余额减少，`wallet_transaction` 有 type=2，`biz_id` 指向 gift_record  
3. 管理端能按用户查到账变  
4. 余额不足时不产生任何账变  

**测试**

- `GiftServiceImplTest`：充值写流水、送礼写流水、余额不足无流水、count 非法  

---

### 6.4 直播回放（MVP）

**表**

```sql
CREATE TABLE IF NOT EXISTS `live_replay` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `live_room_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL COMMENT '主播',
  `title` VARCHAR(100) NOT NULL,
  `cover_url` VARCHAR(500) NULL,
  `play_url` VARCHAR(500) NOT NULL COMMENT '回放点播地址',
  `duration_sec` INT NULL,
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1可用 0转码中 -1删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY `idx_user` (`user_id`),
  KEY `idx_room` (`live_room_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='直播回放';
```

**逻辑**

1. 下播时：若 SRS 录制文件存在（或配置了录制回调），生成 `play_url`，插入 `live_replay`  
2. 接口：`GET /pp/live/replays?userId=` 分页；`GET /pp/live/replays/{id}`  
3. 前台：主播空间 Tab「回放」；广场房间卡片「看回放」  
4. 无录制环境：下播不插入，接口返回空，不报错  

**验收**

1. 有录制文件时下播出现回放记录，详情页可点播（mp4/flv）  
2. 无录制环境主流程不受影响  

---

### 6.5 P1 自测清单

- [ ] 主播发起 PK → 同意 → 分屏比分 → 结束恢复  
- [ ] 连麦：RTC 开/关两种行为正确  
- [ ] 充值订单 + 账变 + 送礼账变  
- [ ] 管理端流水可查  
- [ ] 回放条件可用  
- [ ] 相关单测绿  

---

## 7. P2 — 社区闭环

### 7.1 数据库（P2 统一脚本）

文件：`bankend/BiliPlus/sql/upgrade_p2_community.sql`

```sql
-- 播放历史
CREATE TABLE IF NOT EXISTS `play_history` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `video_id` BIGINT NOT NULL,
  `progress_sec` INT NOT NULL DEFAULT 0,
  `duration_sec` INT NOT NULL DEFAULT 0,
  `last_play_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_user_video` (`user_id`, `video_id`),
  KEY `idx_user_time` (`user_id`, `last_play_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='播放历史';

-- 通知
CREATE TABLE IF NOT EXISTS `notification` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '接收者',
  `type` TINYINT NOT NULL COMMENT '1评论 2回复 3关注 4审核 5直播开播 6系统',
  `title` VARCHAR(100) NOT NULL,
  `content` VARCHAR(500) NULL,
  `biz_type` VARCHAR(32) NULL COMMENT 'video|comment|live|system',
  `biz_id` BIGINT NULL,
  `from_user_id` BIGINT NULL,
  `is_read` TINYINT NOT NULL DEFAULT 0,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY `idx_user_read` (`user_id`, `is_read`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内通知';

-- 举报
CREATE TABLE IF NOT EXISTS `report` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `reporter_id` BIGINT NOT NULL,
  `target_type` TINYINT NOT NULL COMMENT '1视频 2评论 3弹幕 4用户 5直播间',
  `target_id` BIGINT NOT NULL,
  `reason` TINYINT NOT NULL COMMENT '1违法 2色情 3辱骂 4广告 5其他',
  `detail` VARCHAR(500) NULL,
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0待处理 1已处理 2已驳回',
  `handler_id` BIGINT NULL COMMENT '管理员',
  `handle_remark` VARCHAR(255) NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `handle_time` DATETIME NULL,
  KEY `idx_status` (`status`, `create_time`),
  KEY `idx_target` (`target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='举报';

-- 收藏夹
CREATE TABLE IF NOT EXISTS `favorite_folder` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `name` VARCHAR(50) NOT NULL,
  `is_default` TINYINT NOT NULL DEFAULT 0,
  `is_private` TINYINT NOT NULL DEFAULT 0,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏夹';

ALTER TABLE `video_favorite`
  ADD COLUMN `folder_id` BIGINT NULL COMMENT '收藏夹ID' AFTER `video_id`,
  ADD KEY `idx_folder` (`folder_id`);

-- 动态
CREATE TABLE IF NOT EXISTS `dynamic` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `type` TINYINT NOT NULL COMMENT '1文字 2投稿视频 3转发 4开播',
  `content` VARCHAR(1000) NULL,
  `video_id` BIGINT NULL,
  `origin_dynamic_id` BIGINT NULL,
  `live_room_id` BIGINT NULL,
  `status` TINYINT NOT NULL DEFAULT 1,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY `idx_user_time` (`user_id`, `create_time`),
  KEY `idx_type_status` (`type`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户动态';

CREATE TABLE IF NOT EXISTS `dynamic_like` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `dynamic_id` BIGINT NOT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_user_dyn` (`user_id`, `dynamic_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动态点赞';
```

### 7.2 通知中心

**触发点（本期必须）**

| 事件 | type | 写入时机 |
|------|------|----------|
| 视频下评论 | 1 | `CommentServiceImpl.add` → 通知 UP 主 |
| 回复评论 | 2 | 通知被回复者 |
| 被关注 | 3 | `InteractionServiceImpl.toggleFollow`（仅关注时） |
| 稿件审核结果 | 4 | 管理端 approve/reject/offline |
| 系统公告 | 6 | 管理端创建（可选） |

**接口**

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/pp/notifications` | 分页；`?isRead=0\|1`、`?type=` |
| GET | `/pp/notifications/unread-count` | 红点数字 |
| POST | `/pp/notifications/{id}/read` | 单条已读 |
| POST | `/pp/notifications/read-all` | 全部已读 |
| POST | `/admin/notifications` | 管理端发系统通知 body:{title,content} |

**推送**

- MVP：轮询 unread-count（登录后 Header 每 30s）  
- 增强：复用 `/ws/chat` 或新增 `/ws/notify`，收到消息后 `unread++`  

**前端**

| 组件 | 改动 |
|------|------|
| `Header.vue` | 铃铛点开通知面板（下拉或 `/notifications` 页）；未读红点 |
| 新页 `views/notify/NotificationPage.vue` | 列表、类型筛选、单条/全部已读 |
| 路由 | `/notifications` |
| `MobileTabBar` | 消息未读合并：私信 + 通知 |

**验收**

1. 他人评论我的视频 → 我收到通知且红点+1  
2. 标记已读后红点减少；read-all 清零  
3. 未登录不展示通知入口数据  

**测试**

- `NotificationServiceImplTest`：评论触发、分页、已读、read-all  

---

### 7.3 播放历史

**接口**

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/pp/play-history` | body:`{videoId, progressSec, durationSec}` upsert；登录 |
| GET | `/pp/play-history` | 分页，按 last_play_time 倒序 |
| DELETE | `/pp/play-history/{videoId}` | 删单条 |
| DELETE | `/pp/play-history` | 清空 |
| GET | `/pp/play-history/video/{videoId}` | 查单视频进度（续播用） |

**前端**

- `DanmakuPlayer`：进度节流（≥5s）调 POST；加载时 GET 续播  
- `UserSpace` 新 Tab「历史」；Header 用户菜单入口  
- 与 `player.rememberProgress` 设置联动（P0）  

**验收**

1. 播放约 10s 后刷新页面续播  
2. 个人空间出现历史记录；可删除  
3. 未登录不写历史  

---

### 7.4 收藏夹

**接口**

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/pp/favorite-folders` | 我的收藏夹列表 |
| POST | `/pp/favorite-folders` | `{name, isPrivate}` |
| PUT | `/pp/favorite-folders/{id}` | 改名/隐私 |
| DELETE | `/pp/favorite-folders/{id}` | 删夹（默认夹不可删） |
| POST | `/pp/interaction/favorite` | 扩展 body:`{videoId, folderId?}`；缺省进默认夹 |
| GET | `/pp/favorite-folders/{id}/videos` | 夹内视频分页 |

**行为约定**

- 注册时自动创建默认收藏夹「默认收藏」`is_default=1`  
- 现有 `toggleFavorite` 兼容：无 folderId 时进默认夹  
- 取消收藏：删除 `video_favorite` 对应行  

**前端**

- 视频详情收藏按钮：弹层选夹 / 新建夹  
- 用户空间收藏 Tab：文件夹网格 → 点进视频列表  

**验收**

1. 新建夹 → 收藏进夹 → 夹内可见  
2. 默认夹不可删  
3. 旧收藏数据仍在默认逻辑可用  

---

### 7.5 动态 Feed

**接口**

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/pp/dynamics` | 发动态 `{type=1, content}`；type=2 由投稿系统自动发 |
| GET | `/pp/dynamics/feed` | 关注流：我关注的人 + 我自己，分页 |
| GET | `/pp/dynamics/hot` | 全站动态（广场） |
| GET | `/pp/dynamics/user/{userId}` | 某人动态 |
| POST | `/pp/dynamics/{id}/like` | 点赞 toggle |
| DELETE | `/pp/dynamics/{id}` | 作者本人删除 |

**自动动态**

- 投稿审核通过（`AdminVideoServiceImpl.approve`）→ 为作者插入 `type=2, video_id=?`  
- 开播成功 → 可选 `type=4`  

**前端**

- 路由 `/dynamic`：Tab「关注 / 全站」  
- `UserSpace` 动态 Tab  
- `Header` 导航或 MobileTabBar 入口  
- 卡片：文字/视频封面/点赞数/时间  

**验收**

1. 关注的人发动态后，关注流可见  
2. 审核通过的视频自动出现在作者动态  
3. 点赞数与取消正确  

**测试**

- `DynamicServiceImplTest`：发布校验、feed 关注范围、自动视频动态  

---

### 7.6 举报与内容治理

**用户侧接口**

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/pp/reports` | `{targetType, targetId, reason, detail?}` 同一目标去重（未结案不重复） |

**管理端接口**

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/admin/reports` | 分页，`status` 筛选 |
| POST | `/admin/reports/{id}/handle` | `{status:1\|2, remark}` 处理/驳回；可联动下架视频/删评论 |

**处理联动（MVP）**

- 视频举报成立 → 调用现有视频下架逻辑  
- 评论举报成立 → 软删评论  
- 用户/直播间 → 调用现有 ban / force-stop（若无则仅记录）  

**前端**

- 详情页/评论/用户页「举报」按钮  
- 管理端新菜单「举报处理」  

**验收**

1. 用户可提交举报；重复举报提示  
2. 管理端处理后 status 变更；成立时目标不可见  
3. 操作写入 `admin_operation_log`（见 P3，P2 可先 remark）  

**测试**

- `ReportServiceImplTest`：去重、状态机、鉴权  

---

### 7.7 P2 自测清单

- [ ] 评论→通知红点→已读  
- [ ] 播放历史续播与空间展示  
- [ ] 收藏夹增删改与收藏入口  
- [ ] 关注 Feed 与自动投稿动态  
- [ ] 举报提交与管理端处理  
- [ ] 单测 + 前后端 build  

---

## 8. P3 — 运营与体验增强

### 8.1 管理端数据看板

**接口**

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/admin/stats/overview` | 今日/累计：用户数、投稿数、待审数、在线直播数、礼物收入、弹幕数 |
| GET | `/admin/stats/trend?days=7` | 近 N 日投稿/注册/礼物曲线 |

**前端**

- `biliPlusAdmin` 默认路由改为 `/Home/Dashboard`（原审核可保留）  
- 新组件 `views/Dashboard/Dashboard.vue`：KPI 卡片 + 简单折线（可用 CSS/SVG，不强制 ECharts）  

**验收**

1. 看板数字与库中 count 口径一致（允许近实时）  
2. 无数据时显示 0 不报错  

---

### 8.2 用户管理

**接口**

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/admin/users` | 分页；keyword/状态筛选 |
| GET | `/admin/users/{id}` | 详情（投稿数、关注数、举报数） |
| POST | `/admin/users/{id}/ban` | 封禁 body:`{reason}` → `user.status=0` |
| POST | `/admin/users/{id}/unban` | 解封 |
| PUT | `/admin/users/{id}/role` | 调整 `user.role`（0普通/1UP/2管理员）慎用 |
| POST | `/admin/user/register` | 补全现有 TODO：创建管理员账号 |

**鉴权增强**

- `admin_user` 表增加 `role`：`0超级管 1运营 2审核`（增量 SQL）  
- 审核员仅视频/举报；运营+用户/直播/礼物；超管+账号  

**前端**

- 菜单「用户管理」：列表、封禁/解封、角色  
- `AdminUserController` 注册 TODO 落地 + 对应页面（可仅超管）  

**验收**

1. 封禁用户后 C 端登录失败或 Token 拦截  
2. 非超管不可改角色  
3. 列表分页与搜索正确  

---

### 8.3 管理端操作日志

```sql
CREATE TABLE IF NOT EXISTS `admin_operation_log` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `admin_id` BIGINT NOT NULL,
  `action` VARCHAR(64) NOT NULL COMMENT 'video.approve|user.ban|report.handle|...',
  `target_type` VARCHAR(32) NULL,
  `target_id` BIGINT NULL,
  `detail` VARCHAR(500) NULL,
  `ip` VARCHAR(64) NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY `idx_admin_time` (`admin_id`, `create_time`),
  KEY `idx_action` (`action`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理端操作日志';
```

- AOP 或在 Admin Service 关键操作后手写插入  
- 管理端可选页面「操作日志」只读列表  

---

### 8.4 番剧详情页

**后端**（`AnimeController` 已有 GET list / `/{id}`，核对 VO 是否含 episodes）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/pp/anime/{id}` | 含简介、封面、状态、评分 |
| GET | `/pp/anime/{id}/episodes` | 选集列表（播放地址、集数、标题） |
| POST | `/pp/anime/{id}/follow` | 追番 toggle（表已有 `user_anime_follow`） |

**前端**

- 新页 `views/anime/AnimeDetail.vue`，路由 `/anime/:id`  
- 选集点击 → 内嵌 `DanmakuPlayer`（episode 的 video 地址）  
- `AnimeSquare.vue` 的 `openDetail` 跳真实路由  

**验收**

1. 方格点击进详情，选集可播  
2. 追番状态往返正确  

---

### 8.5 推荐增强（轻量）

**规则（无算法服务）**

```text
score = w1 * 热度(播放+点赞*3+评论*5) 
      + w2 * 同分类加成 
      + w3 * 关注作者加成 
      + 时间衰减
```

- 后端 `VideoServiceImpl.recommend`：默认随机/最新混合上述打分，返回 top N  
- 过滤：`status=1`、非自己已看（可选）  
- 可选 Redis 缓存 5 分钟  

**验收**

1. 推荐列表不再全是“最旧/最新”极端结果  
2. 接口耗时 < 300ms（本地千级数据）  

---

### 8.6 移动端体验

| 项 | 改动 |
|----|------|
| 详情页 | `xs` 下推荐列表移到视频下方横滑，不再 `el-col :xs=0` |
| TabBar | 增加「动态」；直播可从首页/频道进 |
| 通知/历史 | 移动可访问 `/notifications`、`/dynamic` |

---

### 8.7 P3 自测清单

- [ ] 看板数据口径正确  
- [ ] 用户封禁/解封/权限  
- [ ] 操作日志可查  
- [ ] 番剧详情可播可追  
- [ ] 推荐结果更合理  
- [ ] 移动布局无“推荐整栏消失”  

---

## 9. 接口总表（汇总）

### 9.1 用户端 `/pp/**`

| 阶段 | 方法 | 路径 | 说明 |
|------|------|------|------|
| P0 | PUT | `/pp/people/my/videos/{id}` | 编辑稿件 |
| P0 | DELETE | `/pp/people/my/videos/{id}` | 删除稿件 |
| P0 | POST | `/pp/people/my/videos/{id}/resubmit` | 驳回重提 |
| P0 | POST | `/pp/videos/{id}/share` | 分享计数（可选） |
| P1 | POST | `/pp/live/wallet/recharge/orders` | 创建充值单 |
| P1 | POST | `/pp/live/wallet/recharge/orders/{orderNo}/pay` | 模拟支付 |
| P1 | GET | `/pp/live/wallet/transactions` | 我的账变 |
| P1 | GET | `/pp/live/replays` | 回放列表 |
| P2 | GET/POST/PUT/DELETE | `/pp/notifications*` | 通知 |
| P2 | POST/GET/DELETE | `/pp/play-history*` | 播放历史 |
| P2 | CRUD | `/pp/favorite-folders*` | 收藏夹 |
| P2 | CRUD | `/pp/dynamics*` | 动态 |
| P2 | POST | `/pp/reports` | 举报 |
| P3 | GET | `/pp/anime/{id}/episodes` 等 | 番剧详情 |

### 9.2 管理端 `/admin/**`

| 阶段 | 方法 | 路径 | 说明 |
|------|------|------|------|
| P1 | GET | `/admin/live/wallet/transactions` | 账变查询 |
| P2 | GET/POST | `/admin/reports`、`.../handle` | 举报 |
| P2 | POST | `/admin/notifications` | 系统通知 |
| P3 | GET | `/admin/stats/overview`、`/trend` | 看板 |
| P3 | GET/POST | `/admin/users*` | 用户管理 |
| P3 | POST | `/admin/user/register` | 管理员注册 |
| P3 | GET | `/admin/operation-logs` | 操作日志 |

**公开 GET 放行**（`JwtTokenPeopleInterceptor`）：

```text
/pp/videos/**（已有）
/pp/comments（已有）
/pp/live/rooms
/pp/live/rooms/{id}
/pp/live/gifts
/pp/anime/**、/pp/anime/{id}/episodes
/pp/dynamics/hot
/pp/banners、/pp/categories
```

其余写操作与个人数据必须登录。

---

## 10. 前端路由增量

| 路径 | 组件 | 阶段 | meta |
|------|------|------|------|
| `/notifications` | `Notify/NotificationPage.vue` | P2 | requiresAuth |
| `/dynamic` | `dynamic/DynamicFeed.vue` | P2 | — |
| `/anime/:id` | `anime/AnimeDetail.vue` | P3 | — |
| `/admin` 看板 | Admin `Dashboard.vue` | P3 | admin token |
| 删除 `/test` `/testTwo` | — | P0 | — |

---

## 11. 管理端菜单增量

```text
首页看板          /Home/Dashboard     P3
视频审核          /Home/VideoShenHe   已有
分类管理          /Home/Category      已有
轮播图管理        /Home/Banner        已有
直播管理          /Home/Live          已有
直播分区          /Home/LiveCategory  已有
礼物管理          /Home/Gift          已有
打赏流水          /Home/GiftRecord    已有
钱包账变          /Home/WalletTx      P1
举报处理          /Home/Report        P2
用户管理          /Home/User          P3
操作日志          /Home/OperationLog  P3
```

---

## 12. 测试计划

### 12.1 单元测试（Mockito，不依赖中间件）

| 测试类 | 阶段 | 覆盖 |
|--------|------|------|
| `PeopleUserMyVideoServiceImplTest` | P0 | 编辑鉴权、重提状态、软删 |
| `GiftServiceImplTest`（扩展） | P1 | 充值订单+账变、送礼账变、失败回滚 |
| `LivePkServiceImplTest`（扩展） | P1 | 发起 PK 完整链路、到期结束 |
| `LiveMicServiceImplTest`（扩展） | P1 | RTC 开关分支 |
| `NotificationServiceImplTest` | P2 | 评论/关注/审核触发、已读 |
| `PlayHistoryServiceImplTest` | P2 | upsert、分页、清空 |
| `FavoriteFolderServiceImplTest` | P2 | 默认夹、删夹约束 |
| `DynamicServiceImplTest` | P2 | feed 范围、自动动态 |
| `ReportServiceImplTest` | P2 | 去重、处理联动 |
| `AdminStatsServiceImplTest` | P3 | 看板聚合字段非空 |
| `AdminUserServiceImplTest` | P3 | 封禁、权限角色 |

### 12.2 接口联调清单（Postman / 手工）

**P0**

1. 改设置 → 播放器行为变化  
2. 我的稿件：改标题 / 重提 / 删除  
3. 分享复制链接  

**P1**

1. 开播 → 发起 PK → 同意 → 送礼比分 → 结束  
2. 连麦申请/同意（RTC on/off）  
3. 创建充值单 → 支付 → 账变 → 送礼 → 主机收入  

**P2**

1. 评论触发通知 → 红点 → 已读  
2. 播放 → 历史 → 续播  
3. 收藏夹与动态流  
4. 举报 → 管理端处理 → 内容下架  

**P3**

1. 看板数字  
2. 封禁用户后登录失败  
3. 番剧选集播放  

### 12.3 非功能

- [ ] 直播页切换路由销毁 flv/WS，无内存泄漏告警  
- [ ] 特效连点不卡死（现有队列回归）  
- [ ] 未登录访问个人接口 401 → 跳登录  
- [ ] 前后端 `build` / `mvn test` 通过  

---

## 13. 风险与对策

| 风险 | 对策 |
|------|------|
| 设置键名前后端不一致 | P0 先冻结 defaults 常量，文档与代码共用 |
| 连麦 RTC 环境难配 | 允许 `rtcAvailable=false` 降级，不阻塞 P1 其他项 |
| 账变与余额不一致 | 送礼/充值同一事务；金额用 BIGINT；对账 SQL 写进管理端 |
| 通知风暴 | 仅关键事件写库；unread 用 Redis 缓存可选 |
| 动态 feed 大表慢查询 | 只查关注列表 IN + 索引 `user_id, create_time`；限制关注数 |
| 改 PK/礼物破坏现网 | 特性开关或独立阶段合并；回归 GiftServiceImplTest |
| 生产密钥泄露 | P0 强制 env；仓库只留 `.env.template` |
| 文档双源漂移 | 本计划接口表为契约源；实现偏差回写本文档 |

---

## 14. 交付物清单

| 交付物 | 路径 |
|--------|------|
| 本开发文档 | `UPGRADE_PLAN.md` |
| P0–P3 SQL 增量 | `bankend/BiliPlus/sql/upgrade_p*.sql` |
| 后端新模块 | `controller/service/mapper`：MyVideo、WalletTx、Notification、PlayHistory、FavoriteFolder、Dynamic、Report、AdminStats、AdminUser… |
| 前台页面 | 设置生效播放器、稿件管理、通知、动态、历史、收藏夹、番剧详情等 |
| 管理端页面 | 看板、用户、举报、账变、日志 |
| 环境模板 | `frontend/biliPlus/.env.*`、`bankend/BiliPlus/.env.template` |
| 单测 | `src/test/java/com/biliplus/service/*Test.java` 按阶段扩展 |
| 回归清单 | 本文档 §12 |

---

## 15. 建议排期与启动顺序

```text
第 1 周    P0 体验收口（并行：配置外置 ∥ 播放设置 ∥ 稿件管理）
第 2 周    P1 PK发起 + 钱包账变（连麦 RTC 可延后 2～3 天）
第 3 周    P2 通知 + 历史 + 收藏夹（动态/举报视人力）
第 4 周    P2 收尾 + P3 看板/用户管理/番剧详情
机动       连麦媒体、回放、推荐增强
```

**建议启动顺序**

1. 确认本文档接口表与 defaults 字段  
2. 执行/编写 `upgrade_p1_wallet.sql` 等增量脚本（可与编码并行）  
3. **从 P0 的播放设置生效 + env 外置开始**（用户可感知 + 降低联调成本）  
4. P1 先做主播发起 PK + 账变（直播闭环价值最高）  
5. P2 通知中心优先于动态（红点感知最强）  

---

## 16. 验收总表（合并前检查）

| 检查项 | 通过标准 |
|--------|----------|
| 编译 | 后端 `mvn -DskipTests compile` + `mvn test` |
| 前端 build | `frontend/biliPlus` 与 `biliPlusAdmin` `npm run build` |
| 主链路 | 浏览 → 播放 → 互动 → 投稿 → 审核 → 直播礼物无回归 |
| 新功能 | 各阶段自测清单勾选完毕 |
| 安全 | 无硬编码密钥；越权接口 403；未登录 401 |
| 文档 | 偏差回写本文档；README 功能列表更新 |

---

## 附录 A — 现有关键文件索引

```text
bankend/BiliPlus/src/main/java/com/biliplus/
  controller/user/LiveController.java
  controller/user/PeopleUserController.java
  controller/user/VideoController.java
  controller/user/InteractionController.java
  controller/user/CommentController.java
  controller/user/DanmakuController.java
  controller/admin/AdminVideoController.java
  controller/admin/AdminLiveController.java
  controller/admin/AdminGiftController.java
  controller/admin/AdminUserController.java
  service/Impl/GiftServiceImpl.java
  service/Impl/LivePkServiceImpl.java
  service/Impl/LiveMicServiceImpl.java
  websocket/LiveWebSocketHandler.java
  config/WebMvcConfiguration.java

frontend/biliPlus/src/
  router/index.ts
  composables/useLocalSettings.ts
  composables/useLiveSocket.ts
  views/home/Main/Video/components/DanmakuPlayer.vue
  views/home/Main/Video/VideoDetail.vue
  views/setting/components/PlayerSettings.vue
  views/setting/components/VideoSettings.vue
  views/live/LiveRoom.vue
  views/live/LiveStart.vue
  views/home/Header.vue

biliPlusAdmin/src/
  router/index.ts
  views/**（审核/分类/轮播/直播/礼物/流水）

biliplus.sql
bankend/BiliPlus/sql/live_full.sql
```

## 附录 B — 名词与状态约定

| 名词 | 约定 |
|------|------|
| 视频 status | `-1删除 0待审 1已通过 2已下架 3驳回`（与现网一致，若实现不同以代码为准并回写） |
| 直播房间 status | `0未开/关播 1直播中 2已结束`（以 `LiveRoomServiceImpl` 为准） |
| 通知 is_read | `0未读 1已读` |
| 举报 status | `0待处理 1成立已处理 2驳回` |
| 钱包 type | `1充值 2送礼支出 3主播收入 4调整` |
| 动态 type | `1文字 2投稿 3转发 4开播` |
| user.role | `0普通 1UP主 2管理员`（C 端）；admin 角色另见 P3 |

---

**文档结束。**

---

## 附录 C — 变更记录（实现回写）

> 本节记录实现过程中对本文档契约的偏离与补充，保持契约可追溯。

### C.1 P0 播放设置：defaults 采用设置页口径

§5.1「建议 defaults」给的是 DPlayer 原生语义值（`danmakuOpacity: 0.8`、`danmakuSpeed: 1`、`danmakuArea: 0.5`）。
实现改为**冻结设置页已有口径**（`danmakuOpacity: 80`、`danmakuSpeed: 'slow'|'normal'|'fast'`、`danmakuArea: 'top'|'half'|'full'`）：

- 设置页已经上线，用户 localStorage 里存的就是这套键；改键名会让老用户设置静默失效
- 百分比与枚举对用户更直观，`danmakuArea` 的三档比 `0.5` 更能表达「顶部/半屏/全屏」
- 契约仍满足 §13 的对策「先冻结 defaults 常量」：新增 `frontend/biliPlus/src/constants/playerSettings.ts`，
  设置页与播放器共用同一份 `PLAYER_SETTINGS_DEFAULTS` 与映射函数
- 读取时通过 `toRatio()` 同时兼容比例口径（`0.8`）与百分比口径（`80`），后端早期写入的数据同样生效

### C.2 P0.1 播放器侧补充说明

DPlayer 1.25 只把 `danmaku.opacity` / `unlimited` 透传到弹幕实例，**字号、显示区域、滚动速度都不在 options 里**。
因此 `DanmakuPlayer.vue` 在挂载后直接作用于 DOM：字号与显示区域写 `.dplayer-danmaku` 内联样式，
速度用带 id 的样式表压过 DPlayer 自带的 5s 关键帧。这是版本限制，不是实现取巧。

### C.3 P0.3 稿件软删与标签

- 软删用 `video.status = -1`（`VideoStatus.DELETED`），与附录 B 一致；新增 `constant/VideoStatus.java` 与
  `AdminVideoServiceImpl` 共用，消除两端各写一份状态常量的隐患
- 投稿标签此前**全链路无效**：`VideoUploadDTO.tags` 收了但从未落库。本期补上 `tag` / `video_tag` 持久化
  （新增 `VideoTagMapper`），投稿与编辑两条路径共用；`GET /pp/people/my/videos` 返回 `MyVideoVO.tags`

### C.4 P0.5 推荐作者昵称

`VideoMapper.recommend()` 由注解 SQL 改为 XML join（返回 `GetListVideoVO`），
补上 `nickname` / `avatar`，消除 §5.5 里「`用户{id}`」的占位显示。

### C.5 P1.3 主播收入账变的口径

`wallet_transaction.balance_after` 表示「变动后余额」，但主播收入记在 `host_income.total_income` 而非
`user_wallet.balance`。处理方式：`biz_type = 'host_income'` 时 `balance_after` 即主播累计收益，
并在建表注释与 `WalletTransaction` 类注释中写明口径由 `biz_type` 决定。
未新增列，避免为单一场景引入冗余字段。

### C.6 P1.2 连麦：本期落地降级方案（方案 B）

`live.srs.rtc-enabled` 默认 `false`，本次实现：

- 后端正准备返回 `mic_ready` 的 `rtcAvailable` 与（配置 RTC 时才有）`token`，信令链路完整可验
- 前端在 `rtcAvailable=false` 时给出明确提示并**不渲染连麦小窗**，修掉「假小窗」
- `rtcAvailable=true` 时展示「连麦已建立，等待音视频接入」文字态，而不是空白 `<video>`

**未做**：真实 WebRTC 推拉流挂接。理由：1v1 连麦需要双向 publish/play 的 SDP 交换，
本机没有可用的 RTC 版 SRS，写了也无法验证，按 §1.2「或明确降级」与 §6.2「可先只验证信令 + token 返回」收口。
开启 RTC 前请先在具备 SRS RTC 的环境补齐挂流代码。

### C.7 P1.4 直播回放依赖录制配置

回放登记由 `live.replay.enabled` 与 `live.replay.play-url-template` 两个开关控制，
默认 `false` + 空模板 → 下播不写回放记录，接口返回空列表，主流程不受影响（与 §6.4 一致）。

### C.8 P2.4 默认收藏夹改为懒创建

§7.4 要求「注册时自动创建默认收藏夹」。实现改为**首次使用收藏夹能力时懒创建**（`ensureDefaultFolder`），
在「列收藏夹 / 收藏视频 / 查夹内视频」三个入口都会先补建。
原因：注册路径因此不必耦合收藏夹服务，而验收项（默认夹不可删、无 folderId 进默认夹、旧数据可用）完全满足。
历史 `video_favorite.folder_id IS NULL` 的数据按默认夹处理，无需数据迁移脚本。

### C.9 P2.5 动态触发点

自动投稿动态落在**管理端审核通过**（`AdminVideoServiceImpl.approve`）而非投稿时，
保证动态里出现的都是已公开视频；写入前用 `type = 2 AND video_id = ?` 去重，重复审核不会产生重复动态。

### C.10 管理端构建：修复阻断性 tsconfig 与遗留页面

§16 要求 `biliPlusAdmin` 也必须 `npm run build` 通过，但该命令在本次改造**之前就是失败的**，原因与本期功能无关：

1. `biliPlusAdmin/tsconfig.json` 把 `baseUrl` / `paths`（`@/*`）写在根配置里，而根配置只有 `references`、
   `files: []`。TS 项目引用**不会继承**被引用项目之外根配置的 compilerOptions，
   于是 `tsconfig.app.json` 完全不知道 `@/*`，所有 `@/...` 导入都报 TS2307。
   已把 `baseUrl` + `paths` 下沉到实际编译 `src` 的 `tsconfig.app.json`。
2. 修复该问题后暴露出的其余历史类型错误：`utils/request.ts` 的 axios 泛型与拦截器签名、
   `VideoShenHe.vue` 的 `el-radio-button :value="null"`、`VideoShenHeDetails.vue` 的
   `el-descriptions column="1"`（应为 `:column="1"`）、`BannerManage.vue` 未使用的 `handleUploadSuccess`、
   `router/index.ts` 未使用的 `from` 参数，以及 `About.vue` 的类型声明缺失。
3. `views/About.vue` 是没有任何菜单/链接入口的分片上传演示页（`/about`），
   与 §5.5 清理 `/test` 同性质，已移入 `biliPlusAdmin/src/_legacy/` 并删除路由。

### C.11 遗留与已知限制

| 项 | 说明 |
|----|------|
| 稿件标签编辑 | 已支持编辑，但换视频源文件仍不支持（与 §5.3 约束一致） |
| 举报联动 | 视频下架、评论软删已联动；用户/直播间举报目前仅记录结论（§7.6 允许） |
| 通知推送 | 采用 Header 30s 轮询 `unread-count`（§7.2 的 MVP 方案），未做 WebSocket 推送 |
| 前端类型检查 | `frontend/biliPlus` 的 `npm run type-check`（vue-tsc）在改造前就存在大量历史报错，本期以 `npm run build` 为准；`biliPlusAdmin` 的 `npm run build` 已含 vue-tsc 且通过 |
| 回放转码 | `live_replay.status` 预留「转码中」，本期直接置为可用，未接转码任务 |
| 未在浏览器实测 | 本轮只做了编译、单测与构建验证，未启动 MySQL/Redis/SRS 跑端到端冒烟，§12.2 的联调清单仍需人工过一遍 |
 实现过程中若接口或表结构必须调整，请在对应章节追加「变更记录」小节，保持契约可追溯。
