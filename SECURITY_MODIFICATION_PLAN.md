# NexusPlay 安全问题修改计划（对照源码）

依据黑盒渗透结论与源码定位，按优先级给出可执行修改项。  
状态说明：**代码**=改业务代码；**配置**=改 yml/.env/部署；**运维**=本机/服务器加固。

---

## 0. 根因对照（渗透现象 → 源码）

| 渗透现象 | 根因位置 | 结论 |
|----------|----------|------|
| MySQL `root/123456` 且 0.0.0.0 | `application.yml` 默认 `DB_PASSWORD:123456`；MySQL 监听 | 默认弱口令 + 运维暴露 |
| Redis 无密码 | 部署未设 `requirepass`；应用未强制校验 | 运维 + 配置 |
| 登录可爆破 | 有限流（5 次/15 分钟）但**无 IP 维度**、无验证码；空参/错误账号名可绕过计数路径 | `PeopleUserServiceImpl` / `AdminUserController` / `LoginRateLimiter` |
| 验证码可爆破 | 发信接口**手动 get 比对**，失败不删 key；成功才 delete | `PeopleUserController` L96–115 |
| 用户搜索泄露 + LIKE 通配 | `PeopleUserMapper.searchByKeyword` 未转义 `%/_`；返回 `UserDTO`（含 email/phone 字段）而非 `UserPublicVO`；GET 公开 | `PeopleUserMapper` / `PeopleUserServiceImpl.searchUsers` / `JwtTokenPeopleInterceptor` |
| 业务码恒 0 | `Result.success=1`、`Result.error=0`；失败仍 HTTP 200 | `result/Result.java` |
| 未鉴权 500 | 运行时异常统一 `Result.error` + 400/500；错误方法未映射 405 | `GlobalExceptionHandler` |
| 推荐分页失效 | `VideoMapper.recommend` **无 LIMIT**；`recommend()` 不接 page/size | `VideoMapper.xml` / `VideoServiceImpl` |
| WS 握手 200 空体 | `JwtHandshakeInterceptor` 失败仅 `return false`，未写 401 | `JwtHandshakeInterceptor` |
| 头像/外链 localhost | `app.external-url` 默认 `http://localhost:8081` | `application.yml` |

---

## P0 立刻做（基础设施）

### P0-1 数据库口令与监听【配置+运维】
- [ ] `application.yml`：去掉 `DB_PASSWORD:123456` 默认值，改为 `${DB_PASSWORD}`（无默认，启动失败优于弱口令）
- [ ] 同步检查 `spring.datasource.password`、`mail.password` 等，禁止弱默认
- [ ] MySQL：改 root 强密码；应用账号最小权限（仅 `biliplus`）；`bind-address=127.0.0.1` 或防火墙
- [ ] Redis：`requirepass` 强密码；应用侧 `spring.data.redis.password`；`rename-command CONFIG ""`

### P0-2 配置模板与文档【配置】
- [ ] `.env.template`：强调生产必须改 JWT/DB/Redis；可加 `REDIS_PASSWORD`
- [ ] `application.yml` / `application-prod.yml`：增加 `spring.data.redis.password: ${REDIS_PASSWORD:}`

---

## P1 高危（应用代码）

### P1-1 验证码失败即作废 + 失败计数【代码】
**文件**：`PeopleUserController`（`/pp/people/email`）、可选 `ValidateCodeServiceImpl`

1. 改为调用 `validateCodeService.validateCode(captchaId, imageCaptcha)`  
   或至少：`getAndDelete` 后比对（**失败也删除**）。
2. 增加失败计数键 `captcha-fail:{captchaId}`，≥3 次强制失效。
3. 发信成功路径保留原有 60s/日限流；**验证码校验失败不要先扣发信限流**（或单独 `captcha-fail` 限流），避免错误实现导致可用性/绕过混乱。  
   （当前顺序是先限流再验码，失败也占额度——建议改为：先验码，成功后再扣发信额度。）

**验收**：同一 `captchaId` 连续 2 次错 → 第 3 次提示「已过期」；正确码只能用一次。

### P1-2 登录加固：IP 限流 + 验证码策略【代码】
**文件**：`LoginRateLimiter`、`PeopleUserServiceImpl.userlogin`、`AdminUserController.login`

1. `LoginRateLimiter` 增加组合键能力，或调用侧增加：
   - `login-ip:{ip}`：例如 20 次 / 15 分钟（全账号合计）
   - 保留 `people-login:{email}` / `admin-login:{account}` 5 次 / 15 分钟
2. 从 `HttpServletRequest` 取真实 IP（注意 `X-Forwarded-For` 仅信任反代时使用）。
3. 连续失败 ≥5 后，登录接口要求图片验证码（或直接锁定到窗口结束）。
4. 限流键命名统一前缀 `rl:`，便于 Redis 运维与监控。
5. `userlogin`：邮箱为空也应计入 IP 限流，避免刷无效请求。

**验收**：同一 IP 对同一邮箱 6 次错误 → 拒绝；换 100 个邮箱在 1 分钟内大量失败也会被 IP 限流挡住。

### P1-3 用户搜索：转义 LIKE + 公开字段 + 登录门槛【代码】
**文件**：`PeopleUserMapper.java`、`PeopleUserServiceImpl.searchUsers`、`PeopleUserController`

1. **LIKE 转义**：在 Service 层对 `keyword` 做  
   `escapeLike(kw)`：将 `\ % _` 转义，SQL 使用 `LIKE CONCAT('%', #{keyword}, '%') ESCAPE '\\'`  
   同样处理：`AdminMemberMapper.xml`、`SensitiveWordMapper`、`AnimeMapper` 等所有 `LIKE CONCAT`。
2. 返回类型改为 **`UserPublicVO`**（已有），禁止 `UserDTO`（含 email/phone 字段位）。
3. 限制：keyword 最短 1–2 字符；结果 ≤20；可选：仅登录可搜。
4. 不要返回可枚举的完整用户目录（`%`/`_` 转义后自然限制）。

**验收**：`keyword=%` / `_` 不再扫全表；响应 JSON 无 email/phone 键。

### P1-4 去掉危险默认配置【配置】
- [ ] `DB_PASSWORD`、`JWT_*`、`EXTERNAL_URL` 默认值改为启动校验：生产 profile 下缺失则 fail-fast
- [ ] `JwtUtil` 已拒绝弱密钥，保持；启动时打印「使用外部配置密钥」而非明文

---

## P2 中危

### P2-1 统一 Result / HTTP 语义【代码，需前后端协同】
**文件**：`Result.java`、`GlobalExceptionHandler`、前端 `request.ts`

**建议契约（二选一，推荐 A）**：
- **A（兼容优先）**：保留 `code=1 成功 / 0 失败`，但：
  - 登录失败 → HTTP 401 + code=0
  - 参数错误 → 400 + code=0
  - 未登录 → 401 + code=0 + msg「请先登录」（避免有的接口 200+请先登录）
  - 404/405/500 与业务 code 对齐
- **B（行业习惯）**：`code=0 成功`，同步改前端与全部 `Result.success/error` 调用（改动面大）

同时：
- [ ] `GlobalExceptionHandler` 增加 `HttpRequestMethodNotSupportedException` → **405** + `Allow` 头
- [ ] `MissingServletRequestParameterException` / `MethodArgumentTypeMismatchException` → 400（勿进 500 兜底）
- [ ] `/pp/people/settings`、`/pp/interaction/my/*` 等：未登录统一 401，不要 200+文案

### P2-2 拦截器路径收紧【代码】
**文件**：`JwtTokenPeopleInterceptor`

1. `path.contains(prefix)` 改为 `startsWith` + 规范化，避免子串误匹配。
2. 建议列入私有 GET：
   - `/pp/live/my-room`
   - `/pp/people/settings`（若走 GET）
   - `/pp/interaction/my/**`
3. 未读数白名单保持精确匹配。

### P2-3 推荐接口分页【代码】
**文件**：`VideoMapper.xml`、`VideoServiceImpl.recommend`、Controller

1. `recommend` SQL 增加 `LIMIT #{offset}, #{size}`（size 默认 20，上限 50）。
2. Controller 接受 `page/size` 并归一化。
3. `PageResult.total` 仅作预估或另查 count，避免全表聚合拖垮。

### P2-4 WebSocket 握手与 token【代码】
**文件**：`JwtHandshakeInterceptor`

1. 失败时 `response.setStatusCode(HttpStatus.UNAUTHORIZED)`（或 403），再 `return false`，避免 200 空响应。
2. 优先从 `Sec-WebSocket-Protocol` / `Authorization` 头取 token；query `token` 仅兼容并注意日志脱敏（勿打全 token）。
3. 校验失败日志只记 userId 有无，不记 token。

### P2-5 外链与头像 URL【配置+代码】
- [ ] 生产 `EXTERNAL_URL` 必须为对外域名
- [ ] 生成头像 URL 时统一走 `app.external-url`，避免写死 `http://localhost:8081`

---

## P3 低危 / 加固

| 项 | 文件 | 修改 |
|----|------|------|
| `/error` status=999 | `GlobalExceptionHandler` / ErrorController | 自定义 `/error` 返回与业务一致的 Result，HTTP 状态保留 |
| 验证码明文打日志 | `ValidateCodeServiceImpl` L36 | 删除 `log.info("生成验证码：{}", codeValue)` |
| 发信接口日志 | `PeopleUserController` | 不打印 captcha 明文（已脱敏）；email 可部分掩码 |
| 管理端注册口 | `AdminUserController` | 保持「未开放」；生产彻底 disable |
| OPTIONS/TRACE | `WebMvcConfiguration` | 确认不暴露 TRACE；OPTIONS 仅 CORS |
| Dev 监听 | `vite.config.ts` / 启动脚本 | 本地开发 `host: 127.0.0.1`；生产不启 Vite |

---

## 建议实施顺序

```text
第 1 批（半天，止血）
  P0-1 数据库/Redis 口令与监听
  P0-2 / P1-4 去掉弱默认配置
  P1-1 验证码 getAndDelete + 失败作废

第 2 批（1 天）
  P1-2 登录 IP 限流 + 验证码策略
  P1-3 搜索 LIKE 转义 + UserPublicVO
  P2-3 recommend LIMIT

第 3 批（1–2 天，前后端）
  P2-1 HTTP/Result 语义统一
  P2-2 拦截器路径
  P2-4 WS 握手状态码
  P3 清理日志与 /error
```

---

## 回归测试清单

- [ ] 同一 captchaId：1 次错误后再校验 → 过期；正确码只能用 1 次
- [ ] 登录：同 IP/同账号 6 次失败被拦；成功后 reset
- [ ] 搜索：`%`、`_`、`admin'--` 无异常、无全表泄露
- [ ] 未登录访问 `/pp/live/my-room`、`/pp/people/settings` → 401
- [ ] `GET /admin/user/login` → 405（非 500）
- [ ] `GET /pp/videos/recommend?size=5` → 恰好 ≤5 条
- [ ] WS 无 token → 非 200 空体；带合法 token → 101
- [ ] 启动无 `DB_PASSWORD` 默认 123456；`.env` 不入库
- [ ] 既有 `mvn test` / 前端 build 通过

---

## 不在本计划内（需单独评审）

- 强制全站登录后才能浏览公开视频（产品策略）
- Result `code` 改为 0=成功（破坏性 API 变更）
- MySQL/Redis 集群化与 TLS
- 敏感词/治理模块业务逻辑加固
