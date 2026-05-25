# Campus Study Hub —— API 清单

> 每个 Phase 新增/变更接口时，对应 owner agent 必须同步追加。字段格式见 docs/TECH_STACK.md §8。
> 所有接口默认 `Content-Type: application/json`。鉴权接口在 `Authorization` 请求头携带 `Bearer <token>`。
> 统一响应壳 `R<T> { code, message, data }`，业务正常时 `code=0`，前端 `utils/request.ts` 自动解包 `data`。

---

## Phase 1 - 鉴权（Agent A）

### POST /api/auth/register
**功能**：学生自助注册账号（默认角色 STUDENT，信誉 100，状态启用）
**鉴权**：放行
**请求**：`RegisterReq`
- `username` *string, required, 3-32*
- `password` *string, required, 6-64*
- `realName` *string, optional*
- `studentNo` *string, optional, 唯一*
- `phone` *string, optional, ^1\d{10}$*
- `email` *string, optional, email 格式*

**响应**：`R<Long>` 新用户 id
**错误码**：400 用户名/学号已存在 / 参数校验失败

### POST /api/auth/login
**功能**：用户名 + 密码登录，签发 JWT
**鉴权**：放行
**请求**：`LoginReq { username, password }`
**响应**：`R<LoginResp>`
```
{
  "token": "<JWT>",
  "user": {
    "id": 3, "username": "stu01", "realName": "张三", "studentNo": "20230001",
    "phone": "13900000001", "email": "s01@csh.edu",
    "role": "STUDENT", "creditScore": 100, "status": 1
  }
}
```
**错误码**：401 用户名或密码错误 / 403 账号已被禁用

### GET /api/auth/me
**功能**：根据 token 返回当前用户最新资料（信誉、状态等会回读 DB）
**鉴权**：登录用户
**请求**：无
**响应**：`R<UserVo>`（结构同 LoginResp.user）
**错误码**：401 未登录或登录已过期

### POST /api/auth/logout
**功能**：占位接口，无状态 JWT 服务器侧无需操作，前端清 token 即可
**鉴权**：放行
**响应**：`R<Void>` (code=0)

---

## WebSocket 通道（Agent A 维护契约）

| 主题 | 方向 | 载荷 |
|---|---|---|
| `/topic/rooms/{roomId}/seats` | 服务端→订阅者 | `SeatPushPayload { seatId, status, updatedAt }` |
| `/user/queue/notifications` | 服务端→指定用户 | `NotificationPayload { id, type, title, content, relatedId, createdAt }` |

> 后端推送统一走 `com.csh.modules.notification.service.WsPushService` 与 `NotificationService`，参见 docs/AGENTS.md §3。

---

## Phase 2 - 自习室与座位（Agent B）

> 状态枚举 `SeatStatus = AVAILABLE | RESERVED | OCCUPIED | FAULT`。
> 学生路径在 `/api/rooms`、`/api/seats`；管理路径在 `/api/admin/rooms`、`/api/admin/seats`。

### GET /api/rooms
**功能**：学生列出开放中的自习室（status=1），含座位汇总（总数/可用数）
**鉴权**：登录用户
**响应**：`R<List<RoomVo>>`
```
{ id, name, location, capacity, openTime, closeTime, status, description, totalSeats, availableSeats }
```

### GET /api/rooms/{id}
**功能**：自习室详情
**鉴权**：登录用户
**响应**：`R<RoomVo>`
**错误码**：400 自习室不存在

### GET /api/seats/by-room/{roomId}
**功能**：列出某自习室全部座位（按 row,col 升序，用于平面图）
**鉴权**：登录用户
**响应**：`R<List<SeatVo>>`
```
{ id, roomId, seatNo, rowNo, colNo, status, feature }
```

### GET /api/seats/{id}
**功能**：单座位详情
**鉴权**：登录用户
**响应**：`R<SeatVo>`

---

### GET /api/admin/rooms
**功能**：管理端列出全部自习室（含已关闭）
**鉴权**：ADMIN
**响应**：`R<List<RoomVo>>`

### POST /api/admin/rooms
**功能**：新建自习室
**鉴权**：ADMIN
**请求**：`CreateRoomReq`
- `name` *string, required, 唯一*
- `location` *string, optional*
- `capacity` *int, ≥0*
- `openTime` *string, "HH:mm", 默认 07:00*
- `closeTime` *string, "HH:mm", 默认 22:30*
- `description` *string, optional*

**响应**：`R<Long>` 新房间 id
**错误码**：400 已存在同名自习室 / 参数校验失败

### PUT /api/admin/rooms/{id}
**功能**：更新自习室（任意字段，null 表示不变；`status` 0=关闭/1=开放）
**鉴权**：ADMIN
**请求**：`UpdateRoomReq`（全字段可选）
**响应**：`R<Void>`

### DELETE /api/admin/rooms/{id}
**功能**：软删自习室（逻辑删除）
**鉴权**：ADMIN
**响应**：`R<Void>`

---

### GET /api/admin/seats/by-room/{roomId}
**功能**：管理端按房间查座位
**鉴权**：ADMIN
**响应**：`R<List<SeatVo>>`

### POST /api/admin/seats
**功能**：新建单个座位
**鉴权**：ADMIN
**请求**：`CreateSeatReq { roomId, seatNo, rowNo, colNo, feature? }`
**响应**：`R<Long>` 新座位 id
**错误码**：400 自习室不存在 / 同房间座位编号重复

### POST /api/admin/seats/batch
**功能**：按 rows × cols 网格批量生成座位（seatNo 格式 `<prefix><row>-<col>`，已存在的跳过）
**鉴权**：ADMIN
**请求**：`BatchCreateSeatReq { roomId, rows≥1, cols≥1, prefix?="A", feature? }`
**响应**：`R<Integer>` 实际新增数量

### PUT /api/admin/seats/{id}
**功能**：更新座位
**鉴权**：ADMIN
**请求**：`UpdateSeatReq { seatNo?, rowNo?, colNo?, status?, feature? }`
**响应**：`R<Void>`

### DELETE /api/admin/seats/{id}
**功能**：软删座位
**鉴权**：ADMIN
**响应**：`R<Void>`

### POST /api/admin/seats/{id}/fault
**功能**：标记座位故障（写 `seat_fault`、置 `seat.status=FAULT`、广播 `/topic/rooms/{roomId}/seats`）
**鉴权**：ADMIN
**请求**：`MarkFaultReq { reason }`
**响应**：`R<Void>`

### POST /api/admin/seats/{id}/fault/clear
**功能**：解除座位故障（关闭 OPEN 状态的 `seat_fault`，并按当前预约状态重算 `seat.status`）
**鉴权**：ADMIN
**响应**：`R<Void>`

### POST /api/admin/seats/{id}/refresh
**功能**：手动触发座位状态重算（按当前 reservation 时段表）
**鉴权**：ADMIN
**响应**：`R<Void>`

---

## Phase 3 - 预约与签到（Agent B）

> 状态枚举 `ReservationStatus = BOOKED | CHECKED_IN | COMPLETED | CANCELLED | EXPIRED`。
> 学生路径 `/api/reservations`；管理路径 `/api/admin/reservations`。
> 校验规则取自 `reservation_rule` 表（max_daily / max_advance_days / min_credit / max_duration_hours / check_in_grace_min / no_show_credit_penalty）。

### POST /api/reservations
**功能**：学生创建预约
**鉴权**：登录用户
**请求**：`CreateReservationReq`
- `seatId` *long, required*
- `startTime` *datetime, required*
- `endTime` *datetime, required*

**响应**：`R<Long>` 新预约 id
**错误码**：4000 参数非法 / 座位故障 / 4001 时段冲突 / 4002 信誉不足 / 4003 超过每日上限 / 4004 超过最长时长/提前天数

### GET /api/reservations/mine?status=&page=&size=
**功能**：当前用户的预约分页（按 startTime 倒序）
**鉴权**：登录用户
**响应**：`R<PageResult<ReservationVo>>`，`ReservationVo { id, userId, username, userRealName, seatId, seatNo, roomId, roomName, startTime, endTime, status, checkInTime, checkOutTime, createdAt }`

### GET /api/reservations/{id}
**功能**：预约详情
**鉴权**：登录用户
**响应**：`R<ReservationVo>`

### POST /api/reservations/{id}/cancel
**功能**：取消自己的预约（仅 BOOKED 状态可取消）
**鉴权**：登录用户
**响应**：`R<Void>`

### POST /api/reservations/{id}/check-in
**功能**：到点签到（窗口：`startTime - grace_min ≤ now ≤ endTime`；状态需为 BOOKED）
**鉴权**：登录用户
**响应**：`R<Void>`

### POST /api/reservations/{id}/check-out
**功能**：签退（CHECKED_IN → COMPLETED）
**鉴权**：登录用户
**响应**：`R<Void>`

---

### GET /api/admin/reservations
**功能**：管理端分页查询预约
**鉴权**：ADMIN
**Query**：`ReservationQuery { page, size, status?, userId?, seatId?, roomId?, startFrom?, startTo? }`
**响应**：`R<PageResult<ReservationVo>>`

### GET /api/admin/reservations/{id}
**功能**：预约详情（管理端）
**鉴权**：ADMIN
**响应**：`R<ReservationVo>`

### POST /api/admin/reservations/{id}/cancel
**功能**：管理员强制取消预约（同时给用户发 RESERVATION_CANCELLED 通知）
**鉴权**：ADMIN
**请求**：`CancelReservationReq { reason }`
**响应**：`R<Void>`

---

## 后台定时任务（Agent B）

| 任务 | 频率 | 作用 |
|---|---|---|
| `ReservationScheduler#handleExpired` | 每 30 s | 把 `startTime + grace_min < now` 仍为 BOOKED 的预约置为 EXPIRED，扣信誉 `no_show_credit_penalty`，发 RESERVATION_EXPIRED 通知 |
| `ReservationScheduler#handleAutoComplete` | 每 30 s | 把 `endTime < now` 仍为 CHECKED_IN 的预约置为 COMPLETED，`checkOutTime = endTime` |

两个任务均会调 `SeatStatusService.refresh(seatId)` 刷新座位并广播 `/topic/rooms/{roomId}/seats`。

---

## Phase 5 - 站内通知 + 用户管理（Agent A）

### 通知类型枚举
```
RESERVATION_CREATED | RESERVATION_CANCELLED | RESERVATION_EXPIRED | RESERVATION_REMINDER
REPORT_FILED | REPORT_RESOLVED | CREDIT_CHANGED | ANNOUNCEMENT | SYSTEM
```

### GET /api/notifications
**功能**：当前用户的通知分页列表（按 createdAt 倒序）
**鉴权**：登录用户
**Query**：`NotificationQuery`
- `page` *int, default 1*
- `size` *int, default 20*
- `readFlag` *0 未读 / 1 已读 / 省略=全部*
- `type` *NotificationType, optional*

**响应**：`R<PageResult<NotificationVo>>`，`NotificationVo { id, type, title, content, readFlag, relatedId, createdAt }`

### GET /api/notifications/unread-count
**功能**：当前用户的未读通知数量
**鉴权**：登录用户
**响应**：`R<Long>`

### PUT /api/notifications/{id}/read
**功能**：把单条通知标记为已读（只能操作自己的）
**鉴权**：登录用户
**响应**：`R<Void>`

### PUT /api/notifications/read-all
**功能**：把当前用户所有未读通知一次性标记为已读
**鉴权**：登录用户
**响应**：`R<Integer>` 实际更新条数

---

### PUT /api/users/me
**功能**：更新当前用户的可编辑资料（realName / phone / email；null 表示不变，空串表示清空）
**鉴权**：登录用户
**请求**：`UpdateProfileReq { realName?, phone?, email? }`
**响应**：`R<UserVo>` 更新后的资料

### POST /api/users/me/password
**功能**：当前用户修改密码（旧密码校验通过才更新）
**鉴权**：登录用户
**请求**：`ChangePasswordReq { oldPassword, newPassword(6-64) }`
**响应**：`R<Void>`
**错误码**：400 旧密码错误 / 参数校验失败

---

### GET /api/admin/users
**功能**：管理端分页查询用户（keyword 模糊匹配 username/realName/studentNo/phone）
**鉴权**：ADMIN
**Query**：`UserQuery { page, size, keyword?, role?, status? }`
**响应**：`R<PageResult<UserVo>>`

### PUT /api/admin/users/{id}/status
**功能**：启用或禁用账号
**鉴权**：ADMIN
**请求**：`UpdateStatusReq { status: 0 | 1 }`
**响应**：`R<Void>`

### POST /api/admin/users/{id}/credit
**功能**：管理员手动调整用户信誉（委托 `CreditService.changeCredit`，会写 credit_log + 发 CREDIT_CHANGED 站内通知）
**鉴权**：ADMIN
**请求**：`AdjustCreditReq { delta: int(±), reason: string }`
**响应**：`R<Integer>` 变更后的信誉分

---

## WS 鉴权（Agent A，Phase 5 落地）

STOMP CONNECT 帧需在 native header 中带 `Authorization: Bearer <jwt>`。
`WebSocketAuthConfig` 解析 JWT 后把 `sub`（用户 ID）设为会话 Principal.name，使
`SimpMessagingTemplate.convertAndSendToUser(userId, "/queue/notifications", payload)` 能正确路由。
前端 `utils/ws.ts` 已在 `connectHeaders` 里带 token；订阅 `/user/queue/notifications` 即可收到个人推送。

---

## Phase 5 - 违规举报（Agent C）

> 状态枚举 `ReportStatus = PENDING | PROCESSING | RESOLVED | REJECTED`。
> 学生路径 `/api/reports`；管理路径 `/api/admin/reports`。
> 处理时如带 `creditDelta != 0` 且 `targetUserId` 存在，会调 `CreditService.changeCredit` 写 credit_log + 发 CREDIT_CHANGED 通知。

### POST /api/reports
**功能**：学生提交举报
**鉴权**：登录用户
**请求**：`CreateReportReq`
- `type` *string, required, 占座/喧哗/设施损坏/其他*
- `description` *string, required, ≤500*
- `targetUserId` *long, optional*
- `reservationId` *long, optional*
- `seatId` *long, optional*
- `evidenceUrl` *string, optional*

**响应**：`R<Long>` 举报 id
**错误码**：400 不能举报自己 / 参数校验失败

### GET /api/reports/mine
**功能**：当前用户的举报分页
**鉴权**：登录用户
**Query**：`ReportQuery { status?, type?, keyword?, page, size }`
**响应**：`R<PageResult<ReportVo>>`，`ReportVo { id, type, description, evidenceUrl, status, result, reporterId, reporterName, targetUserId, targetUserName, reservationId, seatId, handlerId, handlerName, handledAt, createdAt }`

### GET /api/reports/{id}
**功能**：举报详情（仅本人）
**鉴权**：登录用户
**响应**：`R<ReportVo>`
**错误码**：403 无权查看他人举报

### DELETE /api/reports/{id}
**功能**：撤销 PENDING 的举报（软删）
**鉴权**：登录用户
**响应**：`R<Void>`
**错误码**：403 无权撤销他人举报 / 400 仅待处理可撤销

---

### GET /api/admin/reports
**功能**：管理端分页 + 多条件筛选
**鉴权**：ADMIN
**Query**：`ReportQuery { status?, type?, keyword?, reporterId?, targetUserId?, page, size }`
**响应**：`R<PageResult<ReportVo>>`

### GET /api/admin/reports/{id}
**功能**：举报详情
**鉴权**：ADMIN
**响应**：`R<ReportVo>`

### POST /api/admin/reports/{id}/process
**功能**：处理举报（核实通过 / 驳回）
**鉴权**：ADMIN
**请求**：`ProcessReportReq`
- `action` *APPROVE | REJECT*
- `result` *string, optional, ≤500*
- `creditDelta` *int, -50~50, 仅 APPROVE 且 targetUserId 存在时生效*
- `creditReason` *string, optional*

**响应**：`R<Void>`，落 `operation_log`（@OperationLog "举报/处理"）

---

## Phase 6 - 巡检 / 公告 / 规则（Agent C）

### GET /api/admin/inspections
**功能**：管理端巡检记录分页
**鉴权**：ADMIN
**Query**：`InspectionQuery { roomId?, inspectorId?, from?, to?, page, size }`
**响应**：`R<PageResult<InspectionVo>>`，`InspectionVo { id, roomId, roomName, inspectorId, inspectorName, content, issues:[seatId], createdAt }`

### GET /api/admin/inspections/{id}
**鉴权**：ADMIN
**响应**：`R<InspectionVo>`

### POST /api/admin/inspections
**功能**：新增巡检（issues 中的每个 seatId 会被调 `SeatStatusService.markFault`）
**鉴权**：ADMIN
**请求**：`CreateInspectionReq { roomId, content?, issues?:[seatId] }`
**响应**：`R<Long>` 巡检 id；落 `operation_log`（@OperationLog "巡检/新增"）

### DELETE /api/admin/inspections/{id}
**鉴权**：ADMIN
**响应**：`R<Void>`；落 `operation_log`

---

### GET /api/announcements
**功能**：学生端已发布公告分页
**鉴权**：登录用户
**Query**：`AnnouncementQuery { keyword?, page, size }`
**响应**：`R<PageResult<AnnouncementVo>>`，`AnnouncementVo { id, title, content, publisherId, publisherName, status, publishedAt, createdAt, updatedAt }`

### GET /api/announcements/{id}
**功能**：公告详情（仅已发布）
**鉴权**：登录用户
**响应**：`R<AnnouncementVo>`

### GET /api/announcements/active?limit=5
**功能**：当前生效公告 top N，用于首页/Layout 轮播
**鉴权**：登录用户
**响应**：`R<List<AnnouncementVo>>`

---

### GET /api/admin/announcements
**功能**：管理端公告列表（含草稿）
**鉴权**：ADMIN
**Query**：`AnnouncementQuery { keyword?, status?, page, size }`
**响应**：`R<PageResult<AnnouncementVo>>`

### GET /api/admin/announcements/{id}
**鉴权**：ADMIN
**响应**：`R<AnnouncementVo>`

### POST /api/admin/announcements
**功能**：新增公告（publishNow=true 立即发布，false 存草稿）
**鉴权**：ADMIN
**请求**：`CreateAnnouncementReq { title, content, publishNow? }`
**响应**：`R<Long>`；落 `operation_log`

### PUT /api/admin/announcements/{id}
**功能**：更新公告（仅传入字段被改）
**请求**：`UpdateAnnouncementReq { title?, content? }`
**响应**：`R<Void>`；落 `operation_log`

### POST /api/admin/announcements/{id}/publish
**功能**：发布草稿（status=1, published_at=now if first publish）
**响应**：`R<Void>`；落 `operation_log`

### POST /api/admin/announcements/{id}/unpublish
**响应**：`R<Void>`；落 `operation_log`

### DELETE /api/admin/announcements/{id}
**响应**：`R<Void>`；落 `operation_log`

---

### GET /api/rules/current
**功能**：学生读取当前预约规则（用于客户端校验提示）
**鉴权**：登录用户
**响应**：`R<RuleVo>`，`RuleVo { id, maxDaily, maxAdvanceDays, minCredit, checkInGraceMin, maxDurationHours, noShowCreditPenalty, updatedAt }`

### GET /api/admin/rules
**鉴权**：ADMIN
**响应**：`R<RuleVo>`

### PUT /api/admin/rules
**功能**：更新预约规则（6 字段全部 optional，仅传入项被改）
**鉴权**：ADMIN
**请求**：`UpdateRuleReq`
- `maxDaily` *1~20*
- `maxAdvanceDays` *0~30*
- `minCredit` *0~100*
- `checkInGraceMin` *0~120*
- `maxDurationHours` *1~12*
- `noShowCreditPenalty` *0~50*

**响应**：`R<RuleVo>` 最新值；落 `operation_log`

---

## Phase 7 - 数据统计（Agent C）

> 通用 Query：`StatsQuery { from?(yyyy-MM-dd), to?(yyyy-MM-dd), topN?(默认 10) }`；时间窗口闭开区间，`to` 实际转换为次日 00:00。

### GET /api/admin/stats/occupancy
**响应**：`R<List<OccupancyVo>>`，`OccupancyVo { roomId, roomName, capacity, totalReservations, completedReservations, totalSeatHours }`

### GET /api/admin/stats/usage
**响应**：`R<List<UsageVo>>`，`UsageVo { userId, username, realName, studentNo, reservationCount, completedCount, noShowCount, totalHours }`

### GET /api/admin/stats/popular-hours
**响应**：`R<List<PopularHourVo>>`，`PopularHourVo { hour: 0-23, reservationCount }`

### GET /api/admin/stats/violations
**响应**：`R<List<ViolationVo>>`，`ViolationVo { userId, username, realName, studentNo, creditScore, violationCount, totalDeduction }`

### GET /api/admin/stats/faults
**响应**：`R<List<FaultVo>>`，`FaultVo { roomId, roomName, totalFaults, openFaults, latestFaultAt }`

### GET /api/admin/stats/export
**功能**：导出 5 项统计的 xlsx 文件，5 个 Sheet
**鉴权**：ADMIN
**响应**：`application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`，落 `operation_log`（@OperationLog "统计/导出 Excel"）

---

## Phase 8 - 操作日志（Agent C）

> AOP 切面 `OperationLogAspect` 在所有打了 `@OperationLog` 的 controller 方法 @AfterReturning 时写 `operation_log`。
> 注解定义：`@OperationLog(module, action, targetIdSpEL?)`，targetId 通过 SpEL 从方法参数 `#id` 或返回值 `#result?.data` 解析。
> 当前自动落表的操作：Agent C 自己的 5 个 admin controller 的写接口（举报处理、巡检新增/删除、公告 CRUD+发布/下架、规则更新、统计导出）。

### GET /api/admin/logs
**功能**：操作日志分页查询
**鉴权**：ADMIN
**Query**：`OperationLogQuery { module?, action?, username?, userId?, from?, to?, page=1, size=20 }`
**响应**：`R<PageResult<OperationLogVo>>`，`OperationLogVo { id, userId, username, module, action, targetId, ip, ua, createdAt }`

