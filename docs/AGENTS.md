# Campus Study Hub —— 3 Agent 并行开发分工

> 协作前必读：[`docs/TECH_STACK.md`](./TECH_STACK.md)（代码约定）+ [`docs/PROGRESS.md`](./PROGRESS.md)（当前进度）
> 全局设计稿：`C:\Users\huanghancheng\.claude\plans\staged-skipping-lobster.md`

---

## 1. 分工总览

按**业务模块**切边界，不按 Phase 切（Phase 之间有依赖会串行）。三个 agent 文件无交集，可以同时跑。

| Agent | 角色 | 后端模块 | 前端页面 | 跨模块 service 责任 |
|---|---|---|---|---|
| **A（地基组）** | 鉴权 + 通知 + 前端骨架 | `user`、`notification` | `auth/*`、`student/Profile`、`student/Notifications`、`admin/Users` + **前端 Vite 工程骨架**（layouts、router、axios、Pinia user/ws store） | `NotificationService`、`WsPushService` |
| **B（核心业务组）** | 房间 + 预约 + 推荐 | `room`、`reservation`、`recommendation` | `student/Rooms`、`student/SeatMap`、`student/MyReservations`、`student/Recommend`、`admin/Rooms`、`admin/Seats`、`admin/Reservations` | `SeatStatusService` |
| **C（管理后台 + 数据组）** | 举报 + 信誉 + 巡检 + 公告 + 规则 + 日志 + 统计 | `report`、`inspection`、`system`、`statistics`、AOP 切面 | `student/Reports`、`admin/Reports`、`admin/Inspections`、`admin/Announcements`、`admin/Rules`、`admin/Stats`、`admin/Logs` | `CreditService` |

文件归属（不要跨界改）：

```
backend/src/main/java/com/csh/modules/user/        → A
backend/src/main/java/com/csh/modules/notification/ → A
backend/src/main/java/com/csh/modules/room/        → B
backend/src/main/java/com/csh/modules/reservation/ → B
backend/src/main/java/com/csh/modules/report/      → C
backend/src/main/java/com/csh/modules/inspection/  → C
backend/src/main/java/com/csh/modules/system/      → C
backend/src/main/java/com/csh/modules/statistics/  → C
backend/src/main/java/com/csh/scheduler/           → B（ReservationScheduler）
backend/src/main/java/com/csh/aop/                 → C（OperationLogAspect）

frontend/src/utils/, layouts/, router/, stores/user.ts, stores/ws.ts, api/auth.ts, api/user.ts, api/notification.ts, views/auth/, views/student/Profile, views/student/Notifications, views/admin/Users → A
frontend/src/api/{room,reservation,recommend}.ts, views/student/{Rooms,SeatMap,MyReservations,Recommend}, views/admin/{Rooms,Seats,Reservations} → B
frontend/src/api/{report,inspection,announcement,rule,stats,log}.ts, views/student/Reports, views/admin/{Reports,Inspections,Announcements,Rules,Stats,Logs}, components/charts/ → C

docs/PROGRESS.md, docs/API.md → 谁动相关模块谁追加（注意每次 pull 再 commit，避免冲突）
docs/TECH_STACK.md, backend/pom.xml, backend/src/main/java/com/csh/config/SecurityConfig.java, schema.sql → 改之前先在群里同步（不应该有人需要改这几个）
```

---

## 2. 执行顺序（依赖时间线）

```
T0 ┐
   ├ Agent A 开始：Phase 0 前端 Vite 骨架（package.json/vite.config/Element Plus/Pinia/双 layout/axios/router 守卫占位）
   │           Phase 1 后端：AuthController(register/login/me) + JwtUtil 接好
   │           Phase 1 后端骨架同时建 NotificationService + WsPushService 空实现
   │
   ├ Agent B 可以立刻开始（与 A 不冲突）：
   │  · Phase 2 后端：StudyRoomController, SeatController, SeatStatusService（B 自己建）
   │  · Phase 3 后端：ReservationController, ReservationService（预留 CreditService/NotificationService 调用点，留 TODO 待联调）
   │
   ├ Agent C 可以立刻开始（与 A B 不冲突）：
   │  · 先建 CreditService 骨架（让 B 能引用）
   │  · Phase 5 后端：ReportController + 信誉奖惩
   │  · Phase 6 后端：InspectionController, AnnouncementController, RuleController
   │
T1 (A 的前端骨架完成后) ─┐
                       ├ B 开始写自己的前端页（依赖 layouts/axios/router/stores/user）
                       └ C 开始写自己的前端页

T2 (A 的 Phase 1 auth 完成 + 三人前端都能进登录后) ─┐
                                              └ 全员补完前端页面，跑全链路冒烟

T3 ─ A 接 Phase 9 收尾（Knife4j 优化 + README + ER 图 + 端到端冒烟脚本）
   ─ C 接 Phase 7 统计 + Phase 8 AOP 操作日志
   ─ B 接 Phase 4 智能推荐 + 定时任务
```

**最小阻塞链**：A 的前端骨架 + auth 接口是其他人前端工作的前置；C 的 `CreditService` 骨架是 B 预约逻辑的前置。其他都能并行。

---

## 3. 跨模块 service 契约（不允许更改签名）

任何 agent 要跨模块调用服务，**只用下面这套签名**。owner 写实现，其余 agent `@Autowired` 用。

### 3.1 `NotificationService`（Agent A owner，包 `com.csh.modules.notification.service`）

```java
public interface NotificationService {
    void send(Long userId, NotificationType type, String title, String content);
    void send(Long userId, NotificationType type, String title, String content, Long relatedId);
}
```
写入 `notification` 表 + 调 `WsPushService.publishToUser(userId, payload)` 推 `/user/queue/notifications`。

### 3.2 `WsPushService`（Agent A owner，同包）

```java
public interface WsPushService {
    void publishSeat(Long roomId, Long seatId, SeatStatus status);     // → /topic/rooms/{roomId}/seats
    void publishToUser(Long userId, Object payload);                   // → /user/queue/notifications
}
```

### 3.3 `SeatStatusService`（Agent B owner，包 `com.csh.modules.room.service`）

```java
public interface SeatStatusService {
    // 根据 reservation 时段表重新计算座位状态，写回 seat.status，并 publishSeat 广播
    void refresh(Long seatId);
    void markFault(Long seatId, Long reporterId, String reason);       // 故障标记 + 广播 + 写 seat_fault
    void clearFault(Long seatId);
}
```

### 3.4 `CreditService`（Agent C owner，包 `com.csh.modules.report.service`）

```java
public interface CreditService {
    /** 写 credit_log + 改 sys_user.credit_score；发 CREDIT_CHANGED 通知 */
    int changeCredit(Long userId, int delta, String reason, String relatedType, Long relatedId);
    int getScore(Long userId);
}
```

> Owner agent 必须**在自己第一个 commit 里**把上面 4 个 service 的接口 + 一个最简实现先放进去，让其他 agent 能编译过。后续再丰富实现。

---

## 4. 协作纪律

### Git
- **统一 main 分支，不开 feature 分支**（项目轻量，分支合并比直接 pull 还麻烦）。
- 每次 commit 前先 `git pull --rebase origin main`，commit 后立刻 `git push origin main`（已配本地 Clash 代理 `127.0.0.1:7897`）。
- 三个 agent 文件无交集，rebase 不会冲突。如果冲突了——说明有人越界了，先停下排查。
- commit message 中文 "动词+对象"，并在结尾标注 agent，例如：

  ```
  [A] 实现 JWT 登录 / 注册接口
  [B] 添加 SeatController CRUD
  [C] 实现 CreditService 与 credit_log 写入
  ```

### 进度同步
- 每完成一个模块（不是单个接口）的工作，更新 `docs/PROGRESS.md` 对应 Phase 状态。
- 新增/变更 API 时**必须**同步追加 `docs/API.md`（Phase 1 由 Agent A 创建该文件，后续追加）。

### 卡壳处理
- 需要跨 agent 的 service 还没建：在自己代码里写 TODO 注释加 `// TODO[depend on CreditService - Agent C]`，先编译通过留空调用。**不要自己另建一个绕开**。
- 接口签名要改：在 `docs/PROGRESS.md` 顶部留 `## 待协调` 一条记录，并 @ 用户拍板。

### 不许做的事
- 不动 `pom.xml`（要加依赖必须先在 PROGRESS.md 登记并经用户确认）。
- 不动 `SecurityConfig.java`（路径鉴权规则已经定，按 `/api/admin/**` 与默认 authenticated 分流，新接口选对路径即可）。
- 不动 `WebSocketConfig.java`（主题前缀已定，新主题就用 `/topic/...` 或 `/queue/...`）。
- 不动 `schema.sql`（要建新表必须用户确认）。
- 不动其他 agent 的模块包。

---

## 5. 三个 agent 的 kickoff 提示词（可直接复制到各自 agent）

### Agent A —— 地基组

```
你是 Campus Study Hub 项目的 Agent A（地基组），负责鉴权 + 通知 + 整个前端工程骨架。

第一步：开新会话后先读完三份文档建立上下文：
  - docs/PROGRESS.md
  - docs/TECH_STACK.md
  - docs/AGENTS.md（特别是第 1 节里你的归属、第 3 节里你 own 的两个 service 签名）

你的总任务（按顺序做）：
  1. Phase 0 收尾：搭 frontend/ Vite 工程骨架（package.json、vite.config.ts、tsconfig.json、main.ts、App.vue、Element Plus、Pinia、Vue Router、axios 封装 utils/request.ts、双 layout 空壳 StudentLayout.vue/AdminLayout.vue、路由 /student/* 与 /admin/* 与 /auth/login 占位页、router guard 占位、user store 占位）。验证：npm run dev 后 /auth/login、/student、/admin 三个路由能渲染。
  2. Phase 1 鉴权：后端 AuthController (register/login/me)、AuthService、登录请求/响应 DTO；前端登录/注册页 + Pinia user store + token 持久化 + 路由守卫真正接上。验证：用 stu01/123456 登录后 /api/auth/me 返回 LoginUser，刷新页不丢登录态。
  3. 在 Phase 1 第一个 commit 里就把 NotificationService 与 WsPushService 的接口和最简实现放进去（让 B C 能编译引用，参见 docs/AGENTS.md 第 3 节签名）。
  4. Phase 5 自己负责的部分：notification 模块（站内消息列表/已读、WS 个人通道推送）+ student/Notifications 前端页 + admin/Users 用户管理。
  5. Phase 9 收尾：Knife4j 接口分组美化、根 README 启动说明、docs/ER.md（mermaid）。

纪律：每个独立单元 commit 一次，commit message 用 "[A] 动词+对象"；commit 完立刻 git push（本地已配 Clash 代理）；每完成一个模块更新 docs/PROGRESS.md 与 docs/API.md。
```

### Agent B —— 核心业务组

```
你是 Campus Study Hub 项目的 Agent B（核心业务组），负责房间 + 预约 + 推荐。

第一步：开新会话后先读完三份文档建立上下文：
  - docs/PROGRESS.md
  - docs/TECH_STACK.md
  - docs/AGENTS.md（特别是第 1 节里你的归属、第 3 节里你 own 的 SeatStatusService 签名 + 你会调用的 NotificationService/CreditService 签名）

等 Agent A 完成 Phase 0 前端骨架后再启动前端部分；后端可以立刻开始。

你的总任务（按顺序做）：
  1. Phase 2 房间座位（后端）：StudyRoomController、SeatController、SeatFault 标记；同 commit 里把 SeatStatusService 接口 + 实现放进去（refresh/markFault/clearFault）。
  2. Phase 2 前端：student/Rooms 房间列表 + student/SeatMap 平面图（订阅 /topic/rooms/{id}/seats 实现无刷新刷色）；admin/Rooms 和 admin/Seats 管理页。
  3. Phase 3 预约 + 签到（后端）：ReservationController、ReservationService（时段冲突校验、信誉门槛 - 调 CreditService.getScore；预约成功调 NotificationService.send；状态改变调 SeatStatusService.refresh）；scheduler/ReservationScheduler 超时释放与自动结束（释放时调 CreditService.changeCredit 扣分 + NotificationService.send）。
  4. Phase 3 前端：student/MyReservations、座位选择交互嵌进 SeatMap、admin/Reservations 列表。
  5. Phase 4 智能推荐：RecommendationService（按 docs/TECH_STACK.md 第 4.9 节占位说明 + 规划稿里的 5 因子加权公式）+ student/Recommend Tab。

纪律：每个独立单元 commit 一次，"[B] 动词+对象"；commit 完立刻 git push；遇到 CreditService 还没建，留 TODO 不要绕开（参见 docs/AGENTS.md 第 4 节）。
```

### Agent C —— 管理后台 + 数据组

```
你是 Campus Study Hub 项目的 Agent C（管理后台 + 数据组），负责举报 + 信誉 + 巡检 + 公告 + 规则 + 操作日志 + 数据统计。

第一步：开新会话后先读完三份文档建立上下文：
  - docs/PROGRESS.md
  - docs/TECH_STACK.md
  - docs/AGENTS.md（特别是第 3 节里你 own 的 CreditService 签名 + 你会用到的 NotificationService 签名）

后端可以立刻开始；前端等 Agent A 的工程骨架就绪。

你的总任务（按顺序做）：
  1. 第一个 commit：CreditService 接口 + 实现（写 credit_log、改 sys_user.credit_score、调 NotificationService.send 发 CREDIT_CHANGED）。让 Agent B 能立刻引用。
  2. Phase 5 举报（后端）：ReportController（学生提交）+ 管理端 ReportController 审核接口（审核通过/驳回触发 CreditService.changeCredit + NotificationService.send）。
  3. Phase 5 前端：student/Reports（提交 + 我的举报）、admin/Reports（处理列表）。
  4. Phase 6 巡检 + 公告 + 规则（后端）：InspectionController（管理端）、AnnouncementController（学生只读 + 管理 CRUD）、RuleController（管理 GET/PUT）。注意 inspection 提到的故障座位要同步调 SeatStatusService.markFault（Agent B 已建）。
  5. Phase 6 前端：admin/Inspections、admin/Announcements、admin/Rules（学生端公告轮播可以做在 StudentLayout 顶部，Agent A 那边的 layout 留过钩子）。
  6. Phase 7 数据统计：后端 5 个聚合接口（occupancy/usage/popular-hours/violations/faults，复杂 SQL 走 mapper/*.xml）+ EasyExcel 导出；前端 admin/Stats 用 vue-echarts 出 5 张图 + 一键下载 xlsx。
  7. Phase 8 操作日志：aop/OperationLogAspect + @OperationLog 注解，切 /api/admin/** 写入 operation_log；admin/Logs 查询页。

纪律：每个独立单元 commit 一次，"[C] 动词+对象"；commit 完立刻 git push；前端的 ECharts 图表建议拆到 components/charts/。
```

---

## 6. 给用户的操作建议

1. **开三个 Claude Code 会话**（三个独立窗口/三个不同 IDE 项目根都行——都指向同一个 `G:/Code/Toys/Campus Study Hub` 目录）。
2. 把上面 §5 三段 kickoff 提示词分别贴给三个 agent。
3. **强烈建议先让 Agent A 跑一段**（前端骨架是 B C 的前置），B C 可以同时开后端工作但前端要等。
4. 三个 agent 各自 `git pull --rebase` 频繁同步，commit 自动 push。
5. 如果某个 agent 反馈说"需要其他 agent 的东西还没建"，告诉它写 TODO 跳过、继续做下一件事，不要让它擅自越界。
6. 每天/每段时间在主会话里 `git log --oneline | head -30` 检查三个 agent 的进度。
