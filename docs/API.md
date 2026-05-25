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
> Phase 2 临时使用 `SimpMessagingTemplate` 直接广播 `/topic/rooms/{roomId}/seats`；等 A 完成 `WsPushService` 后由 B 替换为 `wsPushService.publishSeat(...)`。

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
