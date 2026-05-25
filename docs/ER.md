# Campus Study Hub —— 数据库 ER 图

> 12 张核心业务表。字符集 `utf8mb4` + collation `utf8mb4_unicode_ci`，全部表带 `id BIGINT AUTO_INCREMENT` + `created_at` + `updated_at`，业务主表带 `deleted TINYINT DEFAULT 0`（MP `@TableLogic` 软删）。
> 枚举字段在 DB 存字符串（`VARCHAR(16/32)`），Java 端绑业务枚举。

---

## 实体关系（mermaid）

```mermaid
erDiagram
    sys_user ||--o{ reservation     : "学生发起预约"
    sys_user ||--o{ report          : "举报发起 / 被举报"
    sys_user ||--o{ notification    : "用户收到通知"
    sys_user ||--o{ credit_log      : "信誉变更日志"
    sys_user ||--o{ inspection      : "管理员巡检"
    sys_user ||--o{ announcement    : "管理员发布公告"
    sys_user ||--o{ operation_log   : "操作行为日志"
    sys_user ||--o{ seat_fault      : "举报座位故障"

    study_room ||--o{ seat          : "房间含座位"
    study_room ||--o{ reservation   : "房间被预约"
    study_room ||--o{ inspection    : "巡检对象"

    seat       ||--o{ reservation   : "座位被预约"
    seat       ||--o{ seat_fault    : "故障记录"
    seat       ||--o{ report        : "举报涉及座位"

    reservation ||--o{ report       : "举报关联预约"

    reservation_rule ||..|| sys_user : "规则被所有学生使用（全局唯一）"

    sys_user {
        BIGINT       id PK
        VARCHAR_64   username "唯一"
        VARCHAR_128  password "BCrypt"
        VARCHAR_64   real_name
        VARCHAR_32   student_no
        VARCHAR_20   phone
        VARCHAR_128  email
        VARCHAR_16   role "STUDENT / ADMIN"
        INT          credit_score "默认 100"
        TINYINT      status "1 启用 0 禁用"
        TINYINT      deleted
        DATETIME     created_at
        DATETIME     updated_at
    }

    study_room {
        BIGINT       id PK
        VARCHAR_64   name
        VARCHAR_128  location
        INT          capacity
        VARCHAR_8    open_time "HH:mm"
        VARCHAR_8    close_time "HH:mm"
        TINYINT      status "1 开 0 关"
        VARCHAR_255  description
        TINYINT      deleted
    }

    seat {
        BIGINT       id PK
        BIGINT       room_id FK
        VARCHAR_16   seat_no
        INT          row_no
        INT          col_no
        VARCHAR_16   status "AVAILABLE/RESERVED/OCCUPIED/FAULT"
        VARCHAR_255  feature "JSON: window/socket/quiet"
        TINYINT      deleted
    }

    reservation {
        BIGINT       id PK
        BIGINT       user_id FK
        BIGINT       seat_id FK
        BIGINT       room_id FK
        DATETIME     start_time
        DATETIME     end_time
        VARCHAR_16   status "BOOKED/CHECKED_IN/COMPLETED/CANCELLED/EXPIRED"
        DATETIME     check_in_time
        DATETIME     check_out_time
        TINYINT      deleted
    }

    report {
        BIGINT       id PK
        BIGINT       reporter_id FK
        BIGINT       target_user_id FK "可空"
        BIGINT       reservation_id FK "可空"
        BIGINT       seat_id FK "可空"
        VARCHAR_32   type "占座/喧哗/设施损坏/其他"
        VARCHAR_500  description
        VARCHAR_500  evidence_url
        VARCHAR_16   status "PENDING/RESOLVED/REJECTED"
        VARCHAR_500  result
        BIGINT       handler_id FK "可空"
        DATETIME     handled_at
        TINYINT      deleted
    }

    credit_log {
        BIGINT       id PK
        BIGINT       user_id FK
        INT          delta "正加负减"
        VARCHAR_255  reason
        VARCHAR_32   related_type "RESERVATION/REPORT/ADMIN_ADJUST"
        BIGINT       related_id
        DATETIME     created_at
    }

    notification {
        BIGINT       id PK
        BIGINT       user_id FK
        VARCHAR_32   type "9 种 NotificationType 枚举"
        VARCHAR_128  title
        VARCHAR_500  content
        TINYINT      read_flag "0 未读 1 已读"
        BIGINT       related_id
        DATETIME     created_at
    }

    inspection {
        BIGINT       id PK
        BIGINT       room_id FK
        BIGINT       inspector_id FK
        VARCHAR_500  content
        VARCHAR_500  issues "JSON 故障座位 id 数组"
        DATETIME     created_at
    }

    announcement {
        BIGINT       id PK
        VARCHAR_128  title
        TEXT         content
        BIGINT       publisher_id FK
        TINYINT      status "1 已发布 0 草稿"
        DATETIME     published_at
        TINYINT      deleted
    }

    reservation_rule {
        BIGINT       id PK
        INT          max_daily "每日最多预约数"
        INT          max_advance_days "最早提前天数"
        INT          min_credit "预约最低信誉"
        INT          check_in_grace_min "迟到宽限"
        INT          max_duration_hours "单次最长"
        INT          no_show_credit_penalty
    }

    seat_fault {
        BIGINT       id PK
        BIGINT       seat_id FK
        BIGINT       reporter_id FK
        VARCHAR_255  description
        VARCHAR_16   status "OPEN/FIXED"
        DATETIME     fixed_at
        DATETIME     created_at
    }

    operation_log {
        BIGINT       id PK
        BIGINT       user_id
        VARCHAR_64   username
        VARCHAR_64   module
        VARCHAR_128  action
        VARCHAR_64   target_id
        VARCHAR_45   ip
        VARCHAR_255  ua
        DATETIME     created_at
    }
```

---

## 关键索引

| 表 | 索引 | 用途 |
|---|---|---|
| `sys_user` | `uk_username` 唯一 | 登录查询 |
| `sys_user` | `idx_student_no` | 学号查找 |
| `seat` | `uk_room_seat (room_id, seat_no)` 唯一 | 同房间座位编号唯一 |
| `seat` | `idx_room_status (room_id, status)` | 房间可用座位筛选 |
| `reservation` | `idx_user_status (user_id, status)` | 我的预约 |
| `reservation` | `idx_seat_time (seat_id, start_time, end_time)` | 时段冲突检测 |
| `reservation` | `idx_room_time (room_id, start_time)` | 房间预约排程 |
| `report` | `idx_reporter / idx_target / idx_status` | 举报检索 |
| `credit_log` | `idx_user` | 用户信誉历史 |
| `notification` | `idx_user_read (user_id, read_flag)` | 未读消息查询 |
| `inspection` | `idx_room` | 房间巡检历史 |
| `seat_fault` | `idx_seat / idx_status` | 故障检索 |
| `operation_log` | `idx_module / idx_created` | 日志按模块/时间查 |

---

## 软删字段

下列表使用 `deleted TINYINT DEFAULT 0`（MyBatis-Plus `@TableLogic` 自动逻辑删除）：

`sys_user` · `study_room` · `seat` · `reservation` · `report` · `announcement`

`credit_log` / `notification` / `inspection` / `operation_log` / `seat_fault` 不软删（按业务追加式写入，无更新需求或物理记录）；
`reservation_rule` 全局唯一单行配置，不删除。

---

## 状态枚举速查

| 枚举 | 值 | 字段 |
|---|---|---|
| `Role` | STUDENT / ADMIN | `sys_user.role` |
| `SeatStatus` | AVAILABLE / RESERVED / OCCUPIED / FAULT | `seat.status` |
| `ReservationStatus` | BOOKED / CHECKED_IN / COMPLETED / CANCELLED / EXPIRED | `reservation.status` |
| `ReportStatus` | PENDING / RESOLVED / REJECTED | `report.status` |
| `NotificationType` | RESERVATION_CREATED/CANCELLED/EXPIRED/REMINDER · REPORT_FILED/RESOLVED · CREDIT_CHANGED · ANNOUNCEMENT · SYSTEM | `notification.type` |
