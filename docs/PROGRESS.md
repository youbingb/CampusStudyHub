# Campus Study Hub —— 项目进度

> 上次更新：2026-05-25
> 当前分支：`main`
> 推荐对照阅读的设计稿：`C:\Users\huanghancheng\.claude\plans\staged-skipping-lobster.md`（10 个 Phase 的总规划）
> 多 agent 协作必读：[`docs/TECH_STACK.md`](./TECH_STACK.md)（依赖版本、模块边界、Controller/Service 模板、API 与 WS 约定）+ [`docs/AGENTS.md`](./AGENTS.md)（3 agent 分工、跨模块 service 契约、kickoff 提示词）

---

## 总体状态

**Phase 0（工程脚手架）已完成 —— 后端 + 前端 + 文档全部就绪。Phase 1 鉴权由 Agent A 完成；Phase 2 房间/座位后端由 Agent B 完成。**

| Phase | 内容 | 状态 |
|---|---|---|
| 0 | 后端工程脚手架 / 数据库 schema / 种子数据 / 前端 Vite 工程骨架 | 🟢 完成 |
| 1 | JWT 鉴权（登录/注册/我的资料）+ 通知模块骨架 | 🟢 完成（Agent A） |
| 2 | 自习室与座位 + WebSocket 推送 | 🟡 后端完成（Agent B），前端待做 |
| 3 | 预约 + 签到 + 定时任务（超时释放） | ⚪ 未开始（Agent B） |
| 4 | 智能推荐（规则打分） | ⚪ 未开始（Agent B） |
| 5 | 违规举报 + 站内通知 | ⚪ 未开始（Agent A 通知 + Agent C 举报） |
| 6 | 巡检 + 公告 + 预约规则管理 | ⚪ 未开始（Agent C） |
| 7 | 数据统计 + EasyExcel 导出 | ⚪ 未开始（Agent C） |
| 8 | 操作日志（AOP 切面） | ⚪ 未开始（Agent C） |
| 9 | 收尾：Knife4j 文档 + README + ER 图 | ⚪ 未开始（Agent A） |

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

后端**可以编译并跑通鉴权**（63 个源文件 `mvn -DskipTests compile` ✅，`AuthController` 已挂在 Knife4j），前端**可以启动并完成登录**（用 `stu01/123456` 登录后能拉到 `/api/auth/me`，token 持久化到 localStorage，刷新页面登录态不丢；管理员账号 `admin/123456` 登录后会被路由守卫送进 `/admin/dashboard`）。

业务页面仍是占位（房间、预约、举报、统计等待 Phase 2~9）。

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

Phase 1 已完成，跨模块的 `NotificationService` 与 `WsPushService` 骨架也已落地（B/C 可直接 `@Autowired`）。Phase 2 后端已完成。后续按 [`docs/AGENTS.md`](./AGENTS.md) 分工继续推进：

1. **Agent A**（地基组）：Phase 5 自己负责的部分（notification 模块完整化 + STOMP 鉴权 + student/Notifications 页 + admin/Users 页）+ Phase 9 收尾
2. **Agent B**（核心业务组）：Phase 2 前端（student/Rooms、SeatMap、admin/Rooms、admin/Seats）；Phase 3 预约/签到（调 `NotificationService.send` + `CreditService.getScore/changeCredit`，CreditService 等 C 建）；Phase 4 智能推荐
3. **Agent C**（管理后台 + 数据组）：先建 `CreditService` 骨架，再做 Phase 5 举报+信誉，Phase 6 巡检/公告/规则，Phase 7 数据统计，Phase 8 操作日志

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
- WebSocket 还未接 STOMP 层鉴权：`WsPushService.publishToUser` 调 `convertAndSendToUser(userId, ...)`，但当前没有任何 STOMP `ChannelInterceptor` 把 token 解析成 principal，所以**个人通道消息暂不会真正送达**（DB 写入与广播 `/topic/...` 不受影响）。等 Agent A 做 Phase 5 完整通知模块时补一个 `WebSocketAuthInterceptor` 即可。
- 后端业务接口除 Auth 外仍未实现，Knife4j 页面目前只有 "鉴权" 这一组。
