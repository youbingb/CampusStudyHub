# Campus Study Hub —— 项目进度

> 上次更新：2026-05-25
> 当前分支：`main`
> 推荐对照阅读的设计稿：`C:\Users\huanghancheng\.claude\plans\staged-skipping-lobster.md`（10 个 Phase 的总规划）
> 多 agent 协作必读：[`docs/TECH_STACK.md`](./TECH_STACK.md)（依赖版本、模块边界、Controller/Service 模板、API 与 WS 约定）+ [`docs/AGENTS.md`](./AGENTS.md)（3 agent 分工、跨模块 service 契约、kickoff 提示词）

---

## 总体状态

**Phase 0（工程脚手架）已完成 —— 后端 + 前端 + 文档全部就绪。Phase 1 鉴权由 Agent A 完成；Phase 2 房间/座位（前后端）由 Agent B 完成。**

| Phase | 内容 | 状态 |
|---|---|---|
| 0 | 后端工程脚手架 / 数据库 schema / 种子数据 / 前端 Vite 工程骨架 | 🟢 完成 |
| 1 | JWT 鉴权（登录/注册/我的资料）+ 通知模块骨架 | 🟢 完成（Agent A） |
| 2 | 自习室与座位 + WebSocket 推送 | 🟢 完成（Agent B） |
| 3 | 预约 + 签到 + 定时任务（超时释放） | ⚪ 未开始（Agent B） |
| 4 | 智能推荐（规则打分） | ⚪ 未开始（Agent B） |
| 5 | 违规举报 + 站内通知 | 🟢 完成（A 通知 + C 举报） |
| 6 | 巡检 + 公告 + 预约规则管理 | ⚪ 未开始（Agent C） |
| 7 | 数据统计 + EasyExcel 导出 | ⚪ 未开始（Agent C） |
| 8 | 操作日志（AOP 切面） | ⚪ 未开始（Agent C） |
| 9 | 收尾：Knife4j 文档 + README + ER 图 | 🟢 完成（Agent A） |

---

## 已完成内容（按 commit 时间倒序）

```
[A] Phase 1 鉴权 + Notification/WsPush 骨架（本次）
94da078 修复 MyBatis-Plus 版本
6dc8252 添加全部 Mapper 接口与 UserDetailsServiceImpl
16928d1 添加 12 个 MyBatis-Plus 实体类
e6e59cd 添加种子数据 data.sql
b5a7e97 添加数据库 schema：12 张核心业务表
a35093c 添加 MyBatis-Plus 分页 / Knife4j / WebSocket(STOMP) 配置
6ff064f 添加 JWT 鉴权骨架与 Spring Security 配置
2e32e11 添加 common 包：统一响应、异常、状态枚举
799722e 搭建后端 Spring Boot 3 骨架与 .gitignore
```

### 关键产出
- **构建文件**：`backend/pom.xml`（Spring Boot 3.2.5 + Java 17 + MyBatis-Plus 3.5.5 + JWT + Knife4j + EasyExcel + Lombok）
- **配置**：`backend/src/main/resources/application.yml`（MySQL 数据源、JWT 密钥、推荐权重、Knife4j 中文 UI）
- **数据库**：`backend/src/main/resources/db/schema.sql`（12 张表）+ `data.sql`（2 admin + 5 学生 + 3 房 × 30 座 + 1 公告 + 预约规则）
- **公共层**：`R`、`PageResult`、`BusinessException`、`GlobalExceptionHandler`、5 个业务枚举
- **安全**：`JwtUtil`、`JwtAuthFilter`、`LoginUser`、`LoginUserHolder`、`SecurityConfig`、`UserDetailsServiceImpl`
- **配置类**：`MybatisPlusConfig`（分页插件）、`SwaggerConfig`（Bearer JWT 安全方案）、`WebSocketConfig`（STOMP /ws + SockJS）
- **实体 + Mapper**：12 张表全部就绪，使用 MP `BaseMapper` 内置方法
- **Phase 1 鉴权（user 模块）**：`AuthController`（register/login/me/logout）、`AuthService`、`LoginReq`/`RegisterReq`/`LoginResp`/`UserVo`，BCrypt 校验密码，JWT 24h
- **Phase 1 通知骨架（notification 模块）**：`NotificationService`（写表 + 个人 WS 推送）、`WsPushService`（座位广播 + 个人通道）、`NotificationPayload`/`SeatPushPayload`；契约见 docs/AGENTS.md §3，B/C 可直接 `@Autowired` 引用
- **Phase 2 房间座位（room 模块）**：`StudyRoomController`/`SeatController`（学生 GET）、`AdminStudyRoomController`/`AdminSeatController`（管理 CRUD + 批量建座 + 故障标记）、`RoomService`/`SeatService`/`SeatStatusService`（B own 的跨模块契约，refresh/markFault/clearFault），广播走 A 的 `WsPushService.publishSeat`
- **Phase 2 前端**：`api/room.ts`（学生 + 管理两套接口封装）、`student/Rooms`（房间卡片 + 可用率配色 tag）、`student/SeatMap`（grid 平面图 + 订阅 `/topic/rooms/{id}/seats` 实时刷色 + 座位详情 Drawer，预约按钮预留 Phase 3 接入）、`admin/Rooms`（CRUD + 开关状态）、`admin/Seats`（按房间筛选 + 单建 / 批量 rows×cols 生成 + 编辑 / 删除 / 标记故障 / 解除故障 / 重算）
- **Phase 5A 通知模块**：`NotificationController`（list/unread-count/markRead/markAllRead）；`WebSocketAuthConfig` 实现 STOMP CONNECT 帧 JWT 鉴权（Principal.name=userId），个人通道 `/user/queue/notifications` 正式可达；前端 `api/notification.ts`、`stores/notification.ts`（WS 订阅 + 未读计数 + ElNotification 浮窗）、`student/Notifications.vue`（分页列表 / 类型筛选 / 已读 / 全部已读 / 实时插入新通知）
- **Phase 5A 用户管理**：`UserService` + `UserController`（更新自我资料 PUT /api/users/me / 修改密码）+ `AdminUserController`（分页查询 / 启用禁用 / 手动调信誉，调 C 的 `CreditService.changeCredit`）；前端 `api/user.ts`、`student/Profile.vue`（编辑资料 + 修改密码）、`admin/Users.vue`（列表 / 搜索 / 启停 / 调信誉对话框）；学生 layout 顶 tab 加未读徽章
- **Phase 9 收尾**：`Knife4jGroupConfig` 把接口按 7 个业务组分组（/doc.html 顶部下拉切换）；根 `README.md` 写完整启动说明 + 截图坑位 + agent 分工总览；`docs/ER.md` 用 mermaid 画完整 12 表 ER 图 + 索引表 + 软删表 + 状态枚举速查

### 已就绪的种子账号（默认密码均为 `123456`）

| 用户名 | 角色 | 学号 | 信誉 |
|---|---|---|---|
| admin | ADMIN | – | 100 |
| admin2 | ADMIN | – | 100 |
| stu01 | STUDENT | 20230001 | 100 |
| stu02 | STUDENT | 20230002 | 100 |
| stu03 | STUDENT | 20230003 | 95 |
| stu04 | STUDENT | 20230004 | 100 |
| stu05 | STUDENT | 20230005 | 80 |

---

## 当前能做什么

后端**可以编译并跑通鉴权、房间座位、举报、信誉、通知、用户管理**（102+ 源文件），前端**可以走完登录 → 浏览房间 → 看座位实时状态 → 收消息 → 编辑资料 → 改密码 → 管理端管用户**。

业务剩余主要是 Phase 3 预约/签到、Phase 4 推荐、Phase 6 巡检/公告/规则、Phase 7 统计、Phase 8 操作日志。

### 准备运行环境（一次性）
```bash
# 1. 创建数据库
mysql -uroot -p123456 -e "CREATE DATABASE campus_study_hub CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 2. 导入 schema + 种子
mysql -uroot -p123456 campus_study_hub < "backend/src/main/resources/db/schema.sql"
mysql -uroot -p123456 campus_study_hub < "backend/src/main/resources/db/data.sql"
```

### 跑后端
```bash
cd backend
mvn spring-boot:run
# 启动后 http://localhost:8080/actuator/health  → {"status":"UP"}
# http://localhost:8080/doc.html （Knife4j；目前接口为空）
```

### 跑前端
```bash
cd frontend
npm install            # 首次
npm run dev            # → http://localhost:5173
```

打开 `http://localhost:5173/auth/login` 能看到登录页；`/admin/dashboard` 等需要登录的路由会自动跳回登录页（路由守卫已生效）。

> **MySQL 密码不是 `123456`？** 改 `backend/src/main/resources/application.yml` 里的 `spring.datasource.password`。

---

## 下一步（按优先级）

Phase 1 已完成，Phase 2 房间座位（前后端）已完成。跨模块的 `NotificationService` / `WsPushService` / `CreditService` 都已落地。后续按 [`docs/AGENTS.md`](./AGENTS.md) 分工继续推进：

1. **Agent A**（地基组）：✅ 全部完成（Phase 0/1/5A/9）
2. **Agent B**（核心业务组）：Phase 3 预约/签到（调 `NotificationService.send` + `CreditService.getScore/changeCredit`，所有依赖均已可用）；Phase 4 智能推荐
3. **Agent C**（管理后台 + 数据组）：Phase 5 举报+信誉已完成；继续 Phase 6 巡检/公告/规则，Phase 7 数据统计，Phase 8 操作日志

每个 agent 接手只需把对应模块下"占位 view"的内容替换成真实页面（页面顶部都已注明谁负责）。

---

## 接续指引（新会话开场怎么继续）

1. 读这份 `docs/PROGRESS.md`（你正在看的文件）。
2. **必读** `docs/TECH_STACK.md` —— 多 agent 协作的代码约定与模块边界。
3. 读设计稿 `C:\Users\huanghancheng\.claude\plans\staged-skipping-lobster.md` 复习全局规划与数据库设计。
4. `git log --oneline` 看具体已完成的 commit。
5. 跟用户确认：是继续 Phase 0 收尾（前端脚手架），还是先去做某个特定 Phase / Module。
6. **每完成一个小功能 commit 一次**（项目偏好，参考 memory `feedback_commit_per_small_feature.md`）。
7. **本阶段结束、准备暂停前必须回来更新这份 PROGRESS.md** 再停。
8. commit 之后**主动推到远端** `git push origin main`（用户要求代码必须上 GitHub）。

---

## 已知遗留

- 后端的 `application.yml` 默认密码 `123456`、JWT secret 是 placeholder——上线前需要替换。
