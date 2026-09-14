# BiliPlus 后续开发计划

承接 `FIX_PLAN.md`（主流程修复）。本计划按阶段交付，**每个功能合并前必须通过对应单元测试**。

测试策略：
- Service 层：Mockito 纯单测（不依赖 MySQL/Redis）
- Mapper XML：编译 + 关键 SQL 人工核对；有库时可跑集成测
- 前端：类型/接口契约与后端字段对齐，改动后 `npm run build` 或 dev 冒烟

---

## 阶段 1：分类 CRUD + 视频按分类筛选

### 后端
- [x] `CategoryService` / `CategoryServiceImpl`
- [x] `CategoryMapper`（list / insert / update / softDelete）
- [x] 管理端 `CategoryController`：GET 列表、POST 新增、PUT 修改、DELETE 删除
- [x] 公开 GET `/pp/categories`（首页分类栏）
- [x] `VideoPageQueryDTO` 增加 `categoryId`，分页查询支持筛选

### 前端
- [x] `api/category.ts`
- [x] 首页分类点击筛选视频列表

### 测试
- [x] `CategoryServiceImplTest`：列表/新增校验/修改/删除不存在分类
- [x] `VideoServiceImplTest`：带 categoryId 分页参数透传

---

## 阶段 2：评论回复 + 评论点赞

### 后端
- [x] 评论查询区分顶级/子评论（或返回 parentId 供前端组装）
- [x] 评论点赞：复用 `user_like`（targetType=2）
- [x] `CommentController`：POST `/pp/comments/{id}/like`
- [x] 删除评论时校验无子评论或级联软删

### 前端
- [x] 评论区展示回复列表
- [x] 回复输入、点赞按钮

### 测试
- [x] `CommentServiceImplTest`：发评论、删他人失败、删自己成功、回复 parentId
- [x] `UserLike` 评论点赞 toggle 单测

---

## 阶段 3：视频审核（管理端）

### 后端
- [x] `AdminVideoController`：分页待审列表、通过、下架
- [x] 投稿默认 `status=0`（待审），管理端通过后 `status=1`
- [x] 仅管理员 JWT 可操作

### 前端（可选本期）
- [x] 无管理后台则先只交付 API

### 测试
- [x] 审核通过后列表可见；下架后详情 404/业务异常

---

## 阶段 4：搜索增强

### 后端
- [x] 视频搜索：标题 + 描述模糊，按相关度/时间排序
- [x] 用户搜索：按昵称
- [x] 防注入：仅参数化查询，限制 pageSize

### 前端
- [x] Header 搜索框跳转 `/search?q=`
- [x] 搜索结果页

### 测试
- [x] 搜索参数校验（空关键词、pageSize 上限）

---

## 阶段 5（排队）：设置页与杂项

- 设置子路由：账号安全 / 播放设置（先占位页）
- 邮件发送频率限制（Redis 计数）
- 生产 CORS 域名白名单配置化
- 403/401 统一响应体

---

## 里程碑

| 阶段 | 完成标志 |
|------|----------|
| 1 | 分类接口可增删改查，首页可按分类筛视频；相关单测绿 |
| 2 | 可回复/点赞评论；相关单测绿 |
| 3 | 待审视频不可公开，管理端可上下架；单测绿 |
| 4 | 搜索页可搜视频/用户；参数校验单测绿 |

## 执行约定

1. 每阶段：写代码 → 写测试 → `mvn test` 通过 → 再进入下一阶段  
2. 不引入与阶段无关的大重构  
3. 实体/SQL 以 `biliplus.sql` 为准  
