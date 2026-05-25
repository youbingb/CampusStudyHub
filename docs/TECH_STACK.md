# Campus Study Hub —— 技术栈与开发约定

> 本文件是**多 agent 协作的契约**。任何 agent 在写代码前必须先读完这份文档，按下面的规定执行；要偏离请先在 `docs/PROGRESS.md` 登记并跟用户确认。

---

## 1. 技术栈总览（锁版本）

### 后端
| 项 | 版本 | 备注 |
|---|---|---|
| JDK | 17（构建目标） | 本地装的是 18，向下兼容 |
| Spring Boot | 3.2.5 | 见 `backend/pom.xml` parent |
| MyBatis-Plus | 3.5.5 | 3.5.6+ 拆 jsqlparser 包，已踩坑回退 |
| MySQL Connector | 跟随 SB 3.2 BOM | runtime |
| JJWT | 0.12.5 | 用新版 `Jwts.builder().subject(...)` API |
| Knife4j | 4.5.0 (openapi3-jakarta) | 中文 UI `/doc.html` |
| EasyExcel | 4.0.1 | Phase 7 导出 |
| Lombok | 跟随 SB BOM | `@Data` `@RequiredArgsConstructor` `@Slf4j` |
| Spring Security | 6.x（SB 自带） | 无状态 + JWT 过滤器 |
| Spring WebSocket | 6.x | STOMP over SockJS |

### 前端（**Phase 0 待落地**，下面是已经定好的版本）
| 项 | 版本/包名 | 备注 |
|---|---|---|
| Node | ≥ 20 | 本地 20.17 |
| 包管理器 | **npm**（本机没装 pnpm） | 别擅自换 |
| Vue | ^3.4 | composition API |
| TypeScript | ^5.4 | strict: true |
| Vite | ^5.2 | dev server 5173 |
| Vue Router | ^4.3 | history 模式 |
| Pinia | ^2.1 | 唯一状态管理 |
| Element Plus | ^2.7 | 同时用于学生端 H5 和管理端 |
| ECharts | ^5.5 + vue-echarts ^7 | Phase 7 |
| axios | ^1.7 | 统一封装在 `utils/request.ts` |
| @stomp/stompjs + sockjs-client | ^7 / ^1.6 | 包装在 `utils/ws.ts` |

### 基础设施
- **MySQL 8.0**（本地 `localhost:3306`，库名 `campus_study_hub`，账密 `root / 123456`）
- 不引入 Redis（设计稿已删；如某 agent 想加，需先登记）
- 不上 Docker（本地 IDE 直接跑）

---

## 2. 项目结构（一图流）

```
Campus Study Hub/
├── backend/                         Spring Boot 后端（单模块）
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/csh/
│       │   ├── CampusStudyHubApplication.java
│       │   ├── common/              R / PageResult / 异常 / 枚举常量
│       │   ├── config/              Security / MyBatis-Plus / Swagger / WebSocket
│       │   ├── security/            JwtUtil / JwtAuthFilter / LoginUser / LoginUserHolder
│       │   ├── scheduler/           @Scheduled 定时任务
│       │   ├── aop/                 @OperationLog 切面（Phase 8）
│       │   └── modules/<module>/
│       │       ├── controller/      *Controller.java
│       │       ├── service/         *Service.java （接口+impl 合一，直接 @Service class）
│       │       ├── mapper/          *Mapper.java extends BaseMapper<E>
│       │       ├── entity/          *.java 与表一一对应
│       │       └── dto/             *Req / *Resp / *Query VO
│       └── resources/
│           ├── application.yml
│           ├── db/                  schema.sql, data.sql
│           └── mapper/              复杂 SQL 的 XML（统计模块）
├── frontend/                        Vue 3 单工程双路由（学生 H5 + 管理后台）
│   ├── package.json, vite.config.ts, tsconfig.json, index.html
│   └── src/
│       ├── api/<module>.ts          axios 接口封装
│       ├── components/              共享组件
│       ├── layouts/                 StudentLayout.vue / AdminLayout.vue
│       ├── views/
│       │   ├── auth/                Login / Register
│       │   ├── student/             学生端业务页
│       │   └── admin/               管理端业务页
│       ├── router/index.ts          /student/* + /admin/* + guard
│       ├── stores/                  Pinia（user / notification / ws）
│       ├── utils/                   request.ts / ws.ts / time.ts
│       └── main.ts, App.vue
└── docs/
    ├── PROGRESS.md                  进度（每个阶段必更新）
    ├── TECH_STACK.md                本文（约定不变就别动）
    ├── API.md                       接口清单（Phase 中持续追加）
    └── ER.md                        ER 图（Phase 9 收尾时画）
```

---

## 3. 模块划分（agent 分工建议）

按模块边界并行，不交叉改文件就不会冲突。

| 后端模块 | 包路径 | 主要表 | 对应前端页面 |
|---|---|---|---|
| `user` | `com.csh.modules.user` | sys_user | auth/Login, auth/Register, student/Profile, admin/Users |
| `room` | `com.csh.modules.room` | study_room, seat, seat_fault | student/Rooms, student/SeatMap, admin/Rooms, admin/Seats |
| `reservation` | `com.csh.modules.reservation` | reservation | student/MyReservations, student/Recommend, admin/Reservations |
| `report` | `com.csh.modules.report` | report, credit_log | student/Reports, admin/Reports |
| `notification` | `com.csh.modules.notification` | notification | student/Notifications |
| `inspection` | `com.csh.modules.inspection` | inspection | admin/Inspections |
| `system` | `com.csh.modules.system` | announcement, reservation_rule, operation_log | admin/Announcements, admin/Rules, admin/Logs |
| `statistics` | `com.csh.modules.statistics` | （只读聚合，无独占表） | admin/Stats |

**跨模块依赖**（agent 之间需要约定的接口）：
- `reservation` 状态变更 → 调 `room.SeatStatusService.refresh(seatId)`（待建）
- `reservation`/`report` 触发信誉变更 → 调 `report.CreditService.changeCredit(userId, delta, reason)`（待建）
- 任何业务模块要发站内消息 → 调 `notification.NotificationService.send(userId, type, title, content)`（待建）
- 座位/通知 WS 广播 → 调 `notification.WsPushService.publishSeat(...)` / `publishToUser(...)`（待建）

> 这几个跨模块 service 在第一个用到它的 Phase 里建出来；以后所有 agent 都用同一份，不要复制粘贴自己写一套。

---

## 4. 后端代码约定

### 4.1 包结构与命名

| 类型 | 后缀 | 例 |
|---|---|---|
| 实体 | （无） | `Seat.java` |
| Mapper | `Mapper` | `SeatMapper.java` |
| Service（接口+实现合一） | `Service` | `SeatService.java`（直接打 `@Service` 的 class） |
| Controller | `Controller` | `SeatController.java` |
| 请求 DTO | `Req` | `CreateReservationReq` |
| 响应 DTO | `Resp` 或 `VO` | `SeatVo`, `ReservationDetailResp` |
| 查询条件 | `Query` | `ReservationQuery` |
| 枚举 | （无） | `SeatStatus` |

**包按模块切**，不按 entity/dto/service 分大目录。`modules/<module>/{controller,service,mapper,entity,dto}` 是固定结构。

### 4.2 Controller 模板（必须照抄结构）

```java
package com.csh.modules.room.controller;

import com.csh.common.R;
import com.csh.common.PageResult;
import com.csh.modules.room.dto.*;
import com.csh.modules.room.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "自习室")
@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @Operation(summary = "列出自习室")
    @GetMapping
    public R<List<RoomVo>> list() {
        return R.ok(roomService.listAll());
    }

    @Operation(summary = "新建自习室")
    @PostMapping
    public R<Long> create(@RequestBody @Valid CreateRoomReq req) {
        return R.ok(roomService.create(req));
    }
}
```

**硬性要求**：
- 类上 `@RestController` + `@RequestMapping("/api/...")` + `@RequiredArgsConstructor`
- 字段 `private final XxxService` 注入（不要 `@Autowired`）
- 每个方法返回 `R<T>`，分页用 `R<PageResult<T>>`
- 用 `@Tag` / `@Operation` 给 Knife4j 写中文标签
- 请求体用 `@RequestBody @Valid XxxReq`

### 4.3 路径前缀约定

| 路径 | 谁能访问 | 鉴权 |
|---|---|---|
| `/api/auth/**` | 任何人 | 放行 |
| `/api/admin/**` | 仅 ADMIN | `ROLE_ADMIN` |
| `/api/**` 其余 | 登录用户 | authenticated |
| `/ws/**` | 任何人 | 放行（鉴权在 STOMP 层后续做） |
| `/actuator/health`, `/doc.html`, `/v3/api-docs/**`, `/swagger-ui/**`, `/webjars/**` | 任何人 | 放行 |

> SecurityConfig 已经按上述配好，新建模块直接放对路径就有正确的鉴权。

### 4.4 Service 写法

不分接口/实现，直接 `@Service` 的具体类（除非有多实现需求）：

```java
@Service
@RequiredArgsConstructor
public class RoomService {
    private final StudyRoomMapper studyRoomMapper;
    private final SeatMapper seatMapper;
    // ...
}
```

事务用 `@Transactional(rollbackFor = Exception.class)`，跨表写动作必须加。

### 4.5 异常 / 校验

- 业务错误抛 `BusinessException(message)` 或 `BusinessException(code, message)`，**不要自己 catch 后 `return R.fail()`**——`GlobalExceptionHandler` 已经接管。
- DTO 用 Jakarta Validation 注解（`@NotBlank` `@Min` `@Future` ...），controller 加 `@Valid` 触发。

### 4.6 获取当前用户

```java
Long uid = LoginUserHolder.currentId();      // 可能 null
LoginUser u = LoginUserHolder.requireCurrent(); // 未登录抛 401
```

**禁止**自己从 `SecurityContextHolder` 拿，统一用 `LoginUserHolder`。

### 4.7 分页

```java
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

Page<Reservation> page = new Page<>(query.getPage(), query.getSize());
IPage<Reservation> result = reservationMapper.selectPage(page,
    new LambdaQueryWrapper<Reservation>()
        .eq(query.getStatus() != null, Reservation::getStatus, query.getStatus())
        .orderByDesc(Reservation::getCreatedAt));
return PageResult.of(result);
```

`Query` DTO 必须有 `page`(默认1) 和 `size`(默认10) 字段。

### 4.8 MyBatis-Plus 用法

- 优先用 `LambdaQueryWrapper`，不写 SQL 字符串
- 字段命名 **必须** `lowerCamelCase` 在 Java + `snake_case` 在 DB；MP 自动映射
- 枚举字段直接绑业务枚举（已用 `Role` `SeatStatus` 等），DB 存字符串
- 软删用 `@TableLogic` 已配 `deleted` 字段，调 `removeById` 自动逻辑删除
- 复杂统计 SQL 写 `resources/mapper/<Module>Mapper.xml`，Mapper 接口加方法签名

### 4.9 跨模块调用 / WebSocket 推送（占位）

这些 service 还没建。**第一个用到的 Phase 负责创建**，包路径如下：

| 跨模块服务 | 包路径 | 干什么 |
|---|---|---|
| `SeatStatusService` | `modules.room.service` | 重新计算并刷新 `seat.status`，触发 WS 广播 |
| `CreditService` | `modules.report.service` | 改信誉分 + 写 `credit_log` |
| `NotificationService` | `modules.notification.service` | 写 `notification` 表 + 个人 WS 推送 |
| `WsPushService` | `modules.notification.service` | 房间广播 `/topic/rooms/{id}/seats`、个人 `/user/queue/notifications` |

后续 agent 直接 `@Autowired` 用这些 service，不要再自己写一套。

### 4.10 日期/时间

- 实体字段统一 `java.time.LocalDateTime`，Jackson 已配 `yyyy-MM-dd HH:mm:ss` + `Asia/Shanghai`
- 前后端约定 ISO 字符串或 `yyyy-MM-dd HH:mm:ss`，都能 parse

---

## 5. WebSocket 主题约定

| 主题 | 方向 | 载荷 schema |
|---|---|---|
| `/topic/rooms/{roomId}/seats` | 服务端→所有订阅者 | `{ seatId, status, updatedAt }` |
| `/user/queue/notifications` | 服务端→指定用户 | `{ id, type, title, content, createdAt }` |

前端订阅写法在 `frontend/src/utils/ws.ts` 统一封装；后端推送统一走 `WsPushService`。**不要**自己注册新主题前缀，否则 SimpleBroker 接不到。

---

## 6. 数据库约定

- 字符集 `utf8mb4` + collation `utf8mb4_unicode_ci`（schema.sql 已定）
- 所有表带 `id BIGINT AUTO_INCREMENT` + `created_at` + `updated_at`
- 业务主表（参与逻辑删除的）带 `deleted TINYINT DEFAULT 0`
- 列名 `snake_case`，枚举字段 `VARCHAR(16)` 存枚举名字符串
- 改 schema 走 `db/schema.sql`（不引入 Flyway/Liquibase 以减重）；改完用户需要**手动 drop & 重新导入**

---

## 7. 前端代码约定（Phase 0 落地后补全细节）

### 7.1 已定的硬规则
- 文件名：组件 `PascalCase.vue`，工具/store `camelCase.ts`，路由命名 `kebab-case`
- 一律 composition API + `<script setup lang="ts">`
- 状态全部走 Pinia，**禁止**用 provide/inject 跨页传业务态
- API 调用统一走 `src/api/<module>.ts`，**禁止**在 view 里直接 `axios.get`
- 错误处理在 `utils/request.ts` 拦截器里 `ElMessage.error(r.message)`；业务 service 只关心 `r.data`

### 7.2 路由结构

```
/                 → 重定向到 /student or /admin 取决于角色
/auth/login
/auth/register
/student/         → StudentLayout
  /home
  /rooms
  /rooms/:id     座位平面图
  /reservations  我的预约
  /recommend
  /reports
  /notifications
  /profile
/admin/          → AdminLayout (要 ADMIN role 才能进)
  /dashboard
  /rooms
  /seats
  /reservations
  /reports
  /users
  /inspections
  /announcements
  /rules
  /stats
  /logs
```

路由守卫在 `router/index.ts` 里统一处理：未登录跳 `/auth/login`；学生访问 `/admin/*` → `/student/home`；管理员访问 `/student/*` 允许（管理员也能看学生视角）。

### 7.3 axios 封装契约
- baseURL `/api`（dev 走 vite proxy 到 `http://localhost:8080`）
- 自动注入 `Authorization: Bearer <token>`（从 Pinia user store 取）
- 响应拦截器解包 `R<T>`：`r.code === 0` 时 return `r.data`，否则 `ElMessage.error(r.message)` 并 `Promise.reject`
- 401 自动 `userStore.logout()` + 跳登录页

---

## 8. API 文档维护

每个 Phase 加新接口时，必须同步追加到 `docs/API.md`（按模块分组），字段：

```
### POST /api/reservations
**功能**：学生创建预约
**鉴权**：登录用户
**请求**：CreateReservationReq { seatId, startTime, endTime }
**响应**：R<Long> reservationId
**错误码**：4001 时段冲突 / 4002 信誉不足 / 4003 超出每日上限
```

---

## 9. 启动 / 调试

```bash
# 准备数据库（一次）
mysql -uroot -p123456 -e "CREATE DATABASE campus_study_hub CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql -uroot -p123456 campus_study_hub < backend/src/main/resources/db/schema.sql
mysql -uroot -p123456 campus_study_hub < backend/src/main/resources/db/data.sql

# 后端（任一）
cd backend && mvn spring-boot:run
# 或在 IntelliJ 跑 CampusStudyHubApplication

# 前端（Phase 0 完成后）
cd frontend && npm install && npm run dev
```

端口：后端 8080，前端 dev 5173。Knife4j：http://localhost:8080/doc.html。

---

## 10. 提交 / 协作纪律

- **每完成一个独立单元（一个 Controller / 一个表 / 一个页面）立即 commit**，commit message 中文 "动词+对象"。
- 多 agent 并行时**按模块分工**，避免两个 agent 同时改 `modules/room/`。
- 改本文件、`schema.sql`、`SecurityConfig`、`WebSocketConfig`、`pom.xml` 必须先在 `docs/PROGRESS.md` 登记。
- 每个会话结束/暂停前必须更新 `docs/PROGRESS.md`。
- 不擅自加新依赖、新框架。

---

## 11. 常见陷阱

| 现象 | 原因 | 解决 |
|---|---|---|
| `Could not find artifact com.baomidou:mybatis-plus-jsqlparser:jar:3.5.7` | MP 3.5.6+ 拆包但发包名变了 | 用 3.5.5（已固定）|
| 启动报 `No qualifying bean of type UserDetailsService` | 没建 `UserDetailsServiceImpl` | 已建，照着模式扩 |
| Knife4j 打不开 | 拼成了 `/swagger-ui/index.html` | 用 `/doc.html` |
| WS 前端连不上 | CORS 或 endpoint 路径错 | endpoint 是 `/ws`，前端 `new SockJS('/ws')` |
| JWT 解析报 `WeakKeyException` | secret 太短 | application.yml 里的 secret ≥ 32 字节 |
| `LocalDateTime` JSON 格式乱 | 没用 Jackson 配置 | 别加 `@JsonFormat`，application.yml 已全局配好 |

---

## 12. 不会做的事（写下来防止 agent 偏方向）

- 不引入 Redis、RabbitMQ、ElasticSearch
- 不上 Docker / Kubernetes
- 不做小程序 / App
- 不引入 OAuth / 第三方登录
- 不做支付
- 不做 i18n（中文 only）
- 不做暗黑模式
- 不写单元测试（除非 Phase 9 收尾要求）
