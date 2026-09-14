# BiliPlus 直播完整开发计划

> 范围：开播 / 观看 / 连麦 / PK / 礼物打赏 / 打赏特效  
> 仓库：`bankend/BiliPlus` · `frontend/biliPlus` · `biliPlusAdmin`  
> 日期：2026-09-13  
> 前置：`LIVE_PLAN.md`（MVP 骨架）· `FIX_PLAN.md` · `DEV_PLAN.md`

---

## 1. 目标与边界

### 1.1 本期目标（完整闭环）

| 模块 | 目标 |
|------|------|
| 开播 | 主播开播、拿到推流地址、下播；观众进房观看 |
| 连麦 | 1v1 上麦，主播同意/拒绝，双流小窗 |
| PK | 双主播对战，分屏 + 实时比分 |
| 礼物 | 余额扣费、送礼流水、房间广播 |
| 特效 | 横幅、连击、高价礼物全屏动画 |

### 1.2 明确不做（本期）

- 礼物提现/分账对账  
- 支付网关真实充值（预留接口）  
- CDN、多码率转码  
- 9 人以上连麦、多人 PK  
- WebRTC 超低延迟全链路替换 FLV  

### 1.3 成功标准

1. OBS 推流成功，观众 `/live` 列表可见并能播放  
2. 观众申请连麦，主播同意后双方可见双画面  
3. 两主播 PK，双方观众看到分屏与比分变化  
4. 送礼扣余额正确，双方房间收到特效横幅；连击可显示  
5. 核心 Service 单测通过；关键接口可 Postman 联调  

---

## 2. 现状盘点（与本计划相关）

| 已有 | 位置 | 可复用 |
|------|------|--------|
| 用户 JWT + ThreadLocal | `JwtTokenPeopleInterceptor` / `UserContext` | 开播、送礼鉴权 |
| WebSocket 聊天 | `/ws/chat` + `ChatWebSocketHandler` | 模式参考；直播建议独立 `/ws/live` |
| 弹幕表/API | `danmaku` + `DanmakuController` | 直播弹幕可扩展 |
| 直播表/实体 | `live_room` / `live_user` + Entity | 需加 stream 字段 |
| 管理端壳 | `biliPlusAdmin`（审核/分类/轮播） | 加礼物目录、直播监管 |
| 前端顶栏 | `Header.vue`「直播」菜单 | 改为跳转 `/live` |
| 播放相关依赖 | `hls.js` / `flv.js` / `dplayer` | 观看页用 flv.js |
| 结果封装 | `Result` / `PageResult` | 统一响应 |
| 事务 | Spring `@Transactional` | 扣款写流水 |

| 缺失 | 说明 |
|------|------|
| 推拉流服务 | 需 SRS（Docker） |
| Live 业务代码 | 无 Controller/Service/Mapper |
| 钱包/礼物表 | 无 |
| RTC 连麦 | 无信令、无 RTC 集成 |
| 直播前台页 | 仅导航文案 |

---

## 3. 技术架构

### 3.1 总览

```text
┌────────────┐  RTMP   ┌─────────────┐  HTTP-FLV   ┌──────────────┐
│ 主播 OBS    │ ──────► │     SRS     │ ──────────► │ 观众 flv.js   │
└────────────┘         │  (Docker)   │             └──────────────┘
                       │  + RTC 可选  │
                       └──────┬──────┘
                              │ 状态回调 / 媒体
┌─────────────┐        ┌──────┴──────┐        ┌─────────────────┐
│ biliPlusAdmin│◄──────►│ Spring Boot │◄──────►│ frontend 用户站  │
│ 礼物/直播监管 │  HTTP  │  8081       │  HTTP  │ /live 开播观看   │
└─────────────┘        │             │  WS    │ 特效层 / PK UI   │
                       │  MySQL      │ /ws/live└─────────────────┘
                       │  Redis      │
                       └─────────────┘
```

### 3.2 选型

| 项 | 选择 | 理由 |
|----|------|------|
| 流媒体 | SRS 5.x Docker | 推流简单，支持 RTC 扩展 |
| 观看 | HTTP-FLV + flv.js | 延迟较低，前端已有依赖 |
| 连麦 | SRS RTC 或 LiveKit 1v1 | 1v1 够用；LiveKit 成熟但多服务 |
| 实时 | 独立 `/ws/live` | 与私信隔离，房间广播清晰 |
| 特效 | Vue3 + CSS + Lottie | 横幅轻量，大特效 Lottie |
| 扣款 | MySQL 条件更新 + 事务 | `balance >= cost` 防超扣 |

### 3.3 直播 WS 协议（统一）

客户端 → 服务端：

```json
{ "type": "join", "roomId": 1 }
{ "type": "leave", "roomId": 1 }
{ "type": "chat", "roomId": 1, "content": "666" }
{ "type": "mic_apply" }
{ "type": "mic_accept", "applyUserId": 9, "agree": true }
{ "type": "mic_leave" }
```

服务端 → 客户端（房间广播）：

```json
{ "type": "chat", "roomId": 1, "userId": 3, "nickname": "A", "content": "hi", "ts": 0 }
{ "type": "online", "roomId": 1, "count": 12 }
{ "type": "gift", "roomId": 1, "senderId": 3, "senderName": "A",
  "giftId": 1, "giftName": "小心心", "icon": "/gifts/heart.png",
  "count": 1, "combo": 5, "level": 1 }
{ "type": "pk_start", "roomId": 1, "pkId": 2, "opponentRoomId": 8,
  "opponentName": "B", "opponentPlayUrl": "...", "durationSec": 300 }
{ "type": "pk_score", "pkId": 2, "scoreA": 100, "scoreB": 80 }
{ "type": "pk_end", "pkId": 2, "winnerRoomId": 1, "scoreA": 200, "scoreB": 80 }
{ "type": "mic_apply", "userId": 9, "nickname": "观众X" }
{ "type": "mic_ready", "role": "host|guest", "rtcRoom": "live-1", "token": "..." }
{ "type": "room_close", "roomId": 1 }
```

---

## 4. 数据库设计

> 新脚本：`bankend/BiliPlus/sql/live_full.sql`  
> 已有 `live_room` / `live_user` 见 `biliplus.sql`。

### 4.1 扩展 live_room

```sql
ALTER TABLE live_room
  ADD COLUMN stream_key VARCHAR(64) NULL COMMENT '推流密钥',
  ADD COLUMN play_url VARCHAR(500) NULL COMMENT 'HTTP-FLV 拉流地址',
  ADD COLUMN push_url VARCHAR(500) NULL COMMENT 'RTMP 推流地址',
  ADD COLUMN cover_url VARCHAR(500) NULL,
  ADD UNIQUE KEY uk_stream_key (stream_key);
```

### 4.2 连麦

```sql
CREATE TABLE live_mic_session (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  live_room_id BIGINT NOT NULL,
  host_user_id BIGINT NOT NULL,
  guest_user_id BIGINT NOT NULL,
  status TINYINT NOT NULL DEFAULT 0 COMMENT '0申请 1进行中 2结束 3拒绝',
  start_time DATETIME NULL,
  end_time DATETIME NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_room (live_room_id),
  KEY idx_guest (guest_user_id)
) COMMENT='直播连麦会话';
```

### 4.3 PK

```sql
CREATE TABLE live_pk (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  room_a_id BIGINT NOT NULL,
  room_b_id BIGINT NOT NULL,
  host_a_id BIGINT NOT NULL,
  host_b_id BIGINT NOT NULL,
  status TINYINT NOT NULL DEFAULT 0 COMMENT '0邀请 1进行 2结束 3取消',
  score_a INT NOT NULL DEFAULT 0,
  score_b INT NOT NULL DEFAULT 0,
  start_time DATETIME NULL,
  end_time DATETIME NULL,
  duration_sec INT NOT NULL DEFAULT 300,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_status (status),
  KEY idx_room_a (room_a_id),
  KEY idx_room_b (room_b_id)
) COMMENT='直播PK';
```

### 4.4 礼物与钱包

```sql
CREATE TABLE gift (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL,
  icon_url VARCHAR(255) NOT NULL,
  price INT NOT NULL COMMENT '硬币价格',
  effect_level TINYINT NOT NULL DEFAULT 1 COMMENT '1普通 2中等 3全屏',
  sort_order INT NOT NULL DEFAULT 0,
  status TINYINT NOT NULL DEFAULT 1,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) COMMENT='礼物目录';

CREATE TABLE user_wallet (
  user_id BIGINT PRIMARY KEY,
  balance BIGINT NOT NULL DEFAULT 0,
  version INT NOT NULL DEFAULT 0,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT='用户钱包（硬币）';

CREATE TABLE gift_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  live_room_id BIGINT NOT NULL,
  pk_id BIGINT NULL COMMENT '若在PK中则计入比分',
  gift_id BIGINT NOT NULL,
  sender_id BIGINT NOT NULL,
  host_user_id BIGINT NOT NULL,
  unit_price INT NOT NULL,
  count INT NOT NULL DEFAULT 1,
  total_price INT NOT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_room (live_room_id),
  KEY idx_sender (sender_id),
  KEY idx_host (host_user_id)
) COMMENT='打赏流水';

CREATE TABLE host_income (
  user_id BIGINT PRIMARY KEY,
  total_income BIGINT NOT NULL DEFAULT 0,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT='主播收益汇总（提现后续）';
```

### 4.5 初始礼物数据

```sql
INSERT INTO gift (name, icon_url, price, effect_level, sort_order) VALUES
('小心心', '/gifts/heart.png', 10, 1, 1),
('棒棒糖', '/gifts/lollipop.png', 50, 1, 2),
('火箭', '/gifts/rocket.png', 1000, 3, 3),
('嘉年华', '/gifts/carnival.png', 3000, 3, 4);
```

---

## 5. 后端模块设计

包路径建议：`com.biliplus.live`（或并列 `controller/live`、`service/impl` 等，与现有一致）。

### 5.1 配置 application.yml

```yaml
live:
  srs:
    rtmp-host: localhost
    rtmp-port: 1935
    http-flv-base: http://localhost:8080/live
    rtc-enabled: false          # 阶段6打开
  pk:
    default-duration-sec: 300
  gift:
    max-count-per-request: 100
```

### 5.2 接口一览

#### 开播 / 观看

| 方法 | 路径 | 鉴权 | 说明 |
|------|------|------|------|
| POST | `/pp/live/rooms` | 登录 | 开播 body: title, coverUrl, categoryId |
| POST | `/pp/live/rooms/{id}/stop` | 仅主播 | 下播 |
| GET | `/pp/live/rooms` | 公开 | 直播中列表分页 |
| GET | `/pp/live/rooms/{id}` | 公开 | 详情含 playUrl |
| POST | `/pp/live/rooms/{id}/enter` | 登录 | 进房记 live_user |
| POST | `/pp/live/rooms/{id}/leave` | 登录 | 离房 |

#### 礼物

| 方法 | 路径 | 鉴权 | 说明 |
|------|------|------|------|
| GET | `/pp/live/gifts` | 公开 | 礼物目录 |
| GET | `/pp/live/wallet` | 登录 | 查余额 |
| POST | `/pp/live/wallet/recharge` | 登录 | 测试充值 body: amount |
| POST | `/pp/live/rooms/{id}/gifts` | 登录 | 送礼 body: giftId, count |

#### 连麦（HTTP 辅助 + WS 主流程）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/pp/live/rooms/{id}/mic/history` | 连麦记录（可选） |

#### PK

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/pp/live/pk/invite` | { opponentRoomId } |
| POST | `/pp/live/pk/{id}/response` | { agree } |
| GET | `/pp/live/pk/active?roomId=` | 当前 PK 状态 |
| POST | `/pp/live/pk/{id}/end` | 主播结束 |

#### 管理端 `/admin/live/**`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/admin/live/rooms` | 全部/按状态列表 |
| POST | `/admin/live/rooms/{id}/force-stop` | 强制下播 |
| GET/POST/PUT/DELETE | `/admin/gifts` | 礼物目录 CRUD |
| GET | `/admin/gift/records` | 打赏流水查询 |

### 5.3 核心 Service 逻辑

#### LiveRoomService

```text
startLive(userId, title, cover, categoryId):
  若已有 status=1 房间 → 复用或报错「已在直播」
  生成 streamKey (UUID)
  拼 pushUrl / playUrl
  insert live_room status=1
  返回房间 VO

stopLive(roomId, userId):
  校验 host_user_id == userId 或管理员
  status=2, end_time=now
  结束进行中 mic / pk
  WS 广播 room_close
```

#### GiftService（关键）

```text
sendGift(senderId, roomId, giftId, count):
  1. count 校验 1..100
  2. 房间 status=1
  3. 礼物 status=1，单价 price
  4. total = price * count
  5. 开启事务：
       UPDATE user_wallet SET balance = balance - total
         WHERE user_id=? AND balance >= total
       若影响行数=0 → 余额不足，抛业务异常
       INSERT gift_record
       UPDATE host_income SET total_income = total_income + total
       若房间在 PK → 更新 live_pk.score_a/b 并广播 pk_score
  6. 连击：同 sender+giftId 在 5s 内 combo++
  7. WS 广播 gift（含 combo、effectLevel）
```

#### 连麦 LiveMicService

```text
apply(guestId, roomId) → 写 session status=0，WS 通知主播
accept(hostId, sessionId):
  status=1, 生成 rtcRoom=live-{roomId} + 短时 token
  双方 WS mic_ready
leave → status=2，通知双方关闭 RTC
```

#### PK LivePkService

```text
invite(hostA, roomB): 校验双方 status=1 且不在 PK
accept: status=1, 双方 WS pk_start（含对方 playUrl）
addScore(pkId, roomWhich, score)  // 由 GiftService 调用
end: pk_end，恢复单屏
```

### 5.4 拦截器

`JwtTokenPeopleInterceptor` 公开 GET 追加：

```text
/pp/live/rooms
/pp/live/gifts
```

其余 `/pp/live/**` 写操作需登录。

### 5.5 WebSocket LiveWebSocketHandler

- 路径：`/ws/live?token=`
- 握手：复用 JWT 解析
- 房间：`Map<Long, Set<WebSocketSession>> roomSessions`
- 进房绑定 userId ↔ session
- 提供 `broadcast(roomId, json)`

---

## 6. 前端模块设计（用户站）

### 6.1 路由

| 路径 | 组件 | 说明 |
|------|------|------|
| `/live` | `views/live/LiveSquare.vue` | 直播广场 |
| `/live/room/:id` | `views/live/LiveRoom.vue` | 观看 + 礼物 + 连麦申请 |
| `/live/start` | `views/live/LiveStart.vue` | 开播（需登录） |

Header「直播」→ `router.push('/live')`。

### 6.2 直播广场 LiveSquare

- 接口：`GET /pp/live/rooms`
- 卡片：封面、标题、主播、在线人数
- 空态：暂无直播 + 「去开播」

### 6.3 开播页 LiveStart

- 表单：标题、封面（复用 `/pp/upload/cover`）、分区
- 点击开播 → 展示 **推流地址 + streamKey**（复制给 OBS）
- 「下播」按钮
- 可选：本地预览 getUserMedia

### 6.4 观看页 LiveRoom（核心）

```text
┌─────────────────────────────────────────┐
│ 房间信息头（主播/标题/在线数）              │
├───────────────────────────┬─────────────┤
│ 视频区 flv.js             │  聊天/弹幕   │
│ （PK 时左右分屏）          │  消息列表    │
│ （连麦时小窗）             │  输入框      │
├───────────────────────────┴─────────────┤
│ 礼物面板 | 连麦申请 | 打赏横幅/全屏特效层   │
└─────────────────────────────────────────┘
```

职责：

1. 进房 `enter` + WS `join`  
2. flv.js 播 `playUrl`  
3. 监听 gift / pk_* / mic_* / chat  
4. 离开 `leave` + 销毁播放器与 WS  

### 6.5 礼物特效层

| 等级 | 表现 |
|------|------|
| effectLevel 1 | 底部横幅 2s：头像 + 名 + 图标 + xN |
| effectLevel 2 | 横幅 3s + 简单光效 |
| effectLevel 3 | 全屏 Lottie 3s（火箭/嘉年华）+ 横幅 |

连击：同人同礼物 5 秒内 `combo` 递增，横幅显示「x 连击」。

实现要点：

- `giftQueue` 串行播放，防止叠爆  
- 资源：`public/gifts/*.png` + `lottie-web`  
- 组件：`LiveGiftBanner.vue` / `LiveGiftStage.vue`

### 6.6 连麦 UI

- 观众：「申请连麦」  
- 主播侧（开播页/房间主播模式）：申请列表同意/拒绝  
- 上麦成功：主播页小窗播 guest RTC；观众侧双流或拉合成流  
- 结束：小窗消失  

### 6.7 PK UI

- 主播：「发起 PK」选对战房间/主播  
- 双方观众：左右分屏 + 顶部比分条 + 倒计时  
- 结束：胜负动画，恢复单屏  

### 6.8 API 文件

```text
src/api/live.ts      房间/开播/进出
src/api/gift.ts      目录/余额/送礼
src/api/pk.ts        邀请/响应/状态
src/composables/useLiveSocket.ts
```

---

## 7. 管理端（biliPlusAdmin）

| 菜单 | 路由 | 功能 |
|------|------|------|
| 直播管理 | `/Home/Live` | 列表、强制下播 |
| 礼物管理 | `/Home/Gift` | 目录 CRUD、上下架 |
| 打赏流水 | `/Home/GiftRecord` | 筛选查询 |

侧边栏增加三项；复用现有 `request` + 布局。

---

## 8. 分阶段实施计划

> 每阶段：编码 → 单测 → 自测清单 → 再进入下一阶段  

### 阶段 A：基础设施（0.5 天）

- [ ] Docker 启动 SRS，确认 1935 / 8080  
- [ ] `sql/live_full.sql` 执行  
- [ ] `live` 配置项  

**验收**：SRS 控制台可访问；表创建成功。

---

### 阶段 B：开播 / 列表 / 观看 API（1 天）

- [ ] `LiveRoomMapper` / `LiveUserMapper`  
- [ ] `LiveRoomService` + `LiveController`  
- [ ] 拦截器公开 GET  
- [ ] 单测：开播校验、重复开播、下播鉴权、列表过滤  

**验收**：Postman 开播返回 pushUrl/playUrl；列表仅直播中。

---

### 阶段 C：前台开播 + 广场 + 观看页（1～1.5 天）

- [ ] 路由 + LiveSquare / LiveStart / LiveRoom  
- [ ] flv.js 播放与销毁  
- [ ] Header「直播」可点  
- [ ] 开播复制推流地址  

**验收**：OBS 推流 → 列表出现 → 进房能播；下播列表消失。

---

### 阶段 D：礼物 + 钱包 + 横幅特效（1.5～2 天）

- [ ] gift / wallet / gift_record / host_income  
- [ ] `GiftService` 扣款事务 + 单测（余额不足、并发条件更新）  
- [ ] `LiveWebSocketHandler` + `useLiveSocket`  
- [ ] 礼物面板 + 横幅特效 + 连击  
- [ ] 管理端礼物 CRUD  

**验收**：充值→送礼→余额减少；双方见横幅；连击显示正确。

---

### 阶段 E：PK（1.5～2 天）

- [ ] `live_pk` + invite/accept/end + score  
- [ ] 送礼计入 PK 分  
- [ ] 双方 WS pk_*  
- [ ] 前台分屏 + 比分条 + 倒计时  
- [ ] 单测：邀请校验、比分累加、结束状态  

**验收**：双开直播发起 PK，观众分屏，送礼比分实时变。

---

### 阶段 F：连麦 1v1（2～3 天）

- [ ] `live_mic_session` + 申请/同意/离开  
- [ ] SRS RTC 或 LiveKit 集成  
- [ ] 前台小窗 + 信令处理  
- [ ] 下播/PK 中断时强制结束连麦  

**验收**：观众上麦，主播见小窗画面声音；下麦恢复正常。

---

### 阶段 G：全屏特效与打磨（0.5～1 天）

- [ ] effectLevel=3 Lottie  
- [ ] 特效队列与限流  
- [ ] 管理端直播监管 / 流水  
- [ ] 回归：开播→礼物→PK→连麦  

---

### 总工期（一人全职粗估）

| 阶段 | 天数 |
|------|------|
| A | 0.5 |
| B | 1 |
| C | 1.5 |
| D | 2 |
| E | 2 |
| F | 3 |
| G | 1 |
| **合计** | **约 11 人日** |

两人可并行：后端 B/D/E/F vs 前端 C/D UI/E UI/F UI。

---

## 9. 测试计划

### 9.1 单元测试（Mockito）

| 测试类 | 覆盖 |
|--------|------|
| `LiveRoomServiceImplTest` | 开播/下播/重复开播/鉴权 |
| `GiftServiceImplTest` | 扣款成功、余额不足、count 非法、写流水 |
| `LivePkServiceImplTest` | 邀请、同意、送礼加分、结束 |
| `LiveMicServiceImplTest` | 申请、同意、拒绝、离开 |

### 9.2 接口联调清单

1. 开播 → OBS 推流 → 列表 → 观看  
2. 充值 10000 → 送火箭 → 余额与流水  
3. PK 邀请/接受 → 送礼比分 → 结束  
4. 连麦申请/同意/离开  
5. 未登录开播/送礼 → 401  

### 9.3 前端自测

- 切换页面销毁 flv 与 WS，无内存泄漏告警  
- 弱网：播放错误提示  
- 特效连点不卡死（队列）  

---

## 10. 风险与对策

| 风险 | 对策 |
|------|------|
| 超扣余额 | `WHERE balance >= total` + 事务 |
| 盗推流 | streamKey 唯一 + 可选 SRS 鉴权回调 |
| WS 房间泄漏 | 连接关闭必 leave；下播清空房间 session |
| PK 乱加分 | 分数只在服务端由 gift_record 驱动 |
| 连麦 NAT | 配置 STUN/TURN；失败降级提示 |
| 特效性能 | 队列串行；全屏特效节流 |
| SRS 未起 | 开播前健康检查，友好报错 |

---

## 11. 交付物清单

| 交付物 | 路径 |
|--------|------|
| 本计划 | `LIVE_FULL_PLAN.md` |
| DB 脚本 | `bankend/BiliPlus/sql/live_full.sql` |
| 后端 Live/Gift/PK/Mic | `com.biliplus.*` live 相关 |
| WS | `websocket/LiveWebSocketHandler` |
| 用户站页面 | `frontend/biliPlus/src/views/live/**` |
| 管理端 | `biliPlusAdmin` 直播/礼物/流水 |
| 单测 | `src/test/java/.../Live*Test` / `Gift*Test` |

---

## 12. 建议启动顺序（执行时）

1. 执行阶段 **A → B**（后端可测）  
2. 并行 **C**（页面能看）  
3. **D** 礼物与特效（用户可感知价值最高）  
4. **E PK** → **F 连麦** → **G 打磨**  

确认计划后，可从阶段 A 的 `live_full.sql` + 阶段 B 的 `LiveRoomServiceImpl` 开始写代码。
