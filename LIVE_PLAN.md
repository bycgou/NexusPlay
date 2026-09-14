# BiliPlus 直播开播模块开发计划

承接现状：库表 `live_room` / `live_user`、实体已建；**无接口、无推拉流、无直播页**。  
本计划目标：实现「用户能开播、观众能进房观看」的最小可用闭环（MVP）。

日期：2026-09-13  
测试约定：Service 用 Mockito 单测；有流媒体环境后再做联调。

---

## 0. 架构选型（MVP）

| 项 | 方案 | 说明 |
|----|------|------|
| 流媒体 | **SRS**（本地 Docker 即可） | 推流 RTMP，拉流 HTTP-FLV / HLS |
| 推流鉴权 | 后端签发带过期的 stream key | 防止盗推 |
| 前端拉流 | **flv.js**（HTTP-FLV，延迟低） | 依赖里已有；HLS 可作备选 |
| 实时弹幕 | 复用现有 `/ws/chat` 或独立 `/ws/live` | 二期可加强 |
| 观众数 | `live_user` 进出记录 + Redis 近似计数（可选） | MVP 可先用 DB |

```text
主播 OBS/浏览器 --RTMP--> SRS --HTTP-FLV--> 观众 flv.js
                         ^
                         | 推流鉴权 / 开播状态
                      Spring Boot (8081)
```

本地 SRS 参考（Docker）：

```bash
docker run --rm -it -p 1935:1935 -p 8080:8080 \
  -p 1985:1985 -p 8000:8000/udp \
  ossrs/srs:5
# 推流: rtmp://localhost:1935/live/{stream}
# 拉流: http://localhost:8080/live/{stream}.live.flv
```

---

## 阶段 1：表结构与后端开播 API

### 数据库

在 `live_room` 上增加（执行 `sql/live_room_extend.sql`）：

| 字段 | 说明 |
|------|------|
| `stream_key` | 推流密钥（唯一） |
| `play_url` | 拉流地址（HTTP-FLV） |
| `push_url` | 推流地址（可选，展示给主播） |

### 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/pp/live/rooms` | 开播（创建/复用房间，status=1，生成 streamKey） |
| POST | `/pp/live/rooms/{id}/stop` | 下播（status=2） |
| GET | `/pp/live/rooms` | 直播中列表（status=1，分页） |
| GET | `/pp/live/rooms/{id}` | 房间详情（含 playUrl） |
| POST | `/pp/live/rooms/{id}/enter` | 进房（记 live_user，返回 playUrl） |
| POST | `/pp/live/rooms/{id}/leave` | 离房 |

### 实现清单

- [ ] `LiveRoomMapper` / `LiveUserMapper`
- [ ] `LiveRoomService` + Impl（开播/下播/列表/进出）
- [ ] `LiveController`（`/pp/live/**`）
- [ ] 拦截器：列表/详情 GET 公开；开播/下播/进出需登录
- [ ] 单测：开播校验、重复开播、下播状态、进房

### 验收

- 登录后开播返回 `streamKey` + `playUrl`
- 未登录不能开播
- 列表只含 `status=1`

---

## 阶段 2：推流鉴权 + 配置

### 配置（application.yml）

```yaml
live:
  rtmp-host: localhost
  rtmp-port: 1935
  flv-base-url: http://localhost:8080/live
  stream-key-ttl-hours: 12
```

### 逻辑

- [ ] 开播生成 `streamKey`（UUID 或 HMAC）
- [ ] `pushUrl = rtmp://{host}:{port}/live/{streamKey}`
- [ ] `playUrl = {flv-base-url}/{streamKey}.live.flv`
- [ ] （可选）SRS 鉴权回调：校验 streamKey 未过期且房间 status=1

### 测试

- [ ] 单测 URL 拼接与 key 生成
- [ ] OBS 用返回的 pushUrl 能推出（联调）

---

## 阶段 3：前台「开播 + 观看」页面

### 路由

| 路径 | 页面 |
|------|------|
| `/live` | 直播广场（列表） |
| `/live/room/:id` | 观看间 |
| `/live/start` | 开播页（需登录） |

### 开播页

- [ ] 标题、封面、分区
- [ ] 展示推流地址 / streamKey（可复制给 OBS）
- [ ] 开播 / 下播按钮

### 观看页

- [ ] flv.js 拉 `playUrl`
- [ ] 房间信息、主播、在线人数（简单轮询或 enter/leave）
- [ ] 顶部「直播」导航跳 `/live`
- [ ] 进房调 enter，离开调 leave

### 测试

- [ ] 列表空态、有数据渲染
- [ ] 未登录开播跳登录
- [ ] 观看页播放器错误提示

---

## 阶段 4：直播弹幕（增强）

- [ ] `POST /pp/live/rooms/{id}/danmaku`（鉴权）
- [ ] `GET /pp/live/rooms/{id}/danmaku` 或 WebSocket `/ws/live/{id}`
- [ ] 观看页输入框 + 弹幕层
- [ ] 可先复用现有 `danmaku` 表加 `live_room_id`，或新建 `live_danmaku`

---

## 阶段 5：管理端与审核

- [ ] 管理端「直播管理」：列表、强制下播
- [ ] （可选）开播资格：仅 UP 主 / 需实名
- [ ] 违规关键词、举报（可后排）

---

## 建议排期（工作量粗估）

| 阶段 | 内容 | 粗估 |
|------|------|------|
| 1 | 表 + 后端开播/列表/进出 + 单测 | 0.5–1 天 |
| 2 | SRS 配置 + 推流地址 | 0.5 天 |
| 3 | 开播页 + 观看页 flv.js | 1 天 |
| 4 | 直播弹幕 | 0.5–1 天 |
| 5 | 管理端 | 0.5 天 |

---

## 风险与边界（MVP 明确不做）

| 不做 | 说明 |
|------|------|
| 浏览器 WebRTC 超低延迟 | 成本高，后续可选 |
| 礼物/连麦/PK | 不在 MVP |
| 多码率转码 | SRS 可后配 |
| CDN 分发 | 本地/单机 SRS 即可验证 |
| 回放录制 | 可后加 SRS 录制 |

---

## 验收标准（MVP）

1. 主播登录 → 开播 → 拿到推流地址 → OBS 能推上  
2. 观众打开 `/live` 看到「直播中」列表  
3. 进入直播间能播放画面（HTTP-FLV）  
4. 主播下播后列表消失，观众页提示已结束  
5. 阶段 1 相关 Service 单测全部通过  

---

## 下一步

若确认本计划，建议执行顺序：

1. 先做 **阶段 1 + 2**（后端可测、可推流）  
2. 再做 **阶段 3**（页面能看）  
3. 弹幕与管理端按需排期  

确认后可直接从 `sql/live_room_extend.sql` + `LiveRoomService` 开始实现。
