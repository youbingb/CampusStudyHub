# Campus Study Hub —— 项目进度

> 上次更新：2026-05-25
> 当前分支：`main`
> 推荐对照阅读的设计稿：`C:\Users\huanghancheng\.claude\plans\staged-skipping-lobster.md`（10 个 Phase 的总规划）
> 多 agent 协作必读：[`docs/TECH_STACK.md`](./TECH_STACK.md)（依赖版本、模块边界、Controller/Service 模板、API 与 WS 约定）+ [`docs/AGENTS.md`](./AGENTS.md)（3 agent 分工、跨模块 service 契约、kickoff 提示词）

---

## 总体状态

**Phase 0（工程脚手架）进行中 —— 后端骨架已完成并通过编译，前端骨架尚未开始。**

| Phase | 内容 | 状态 |
|---|---|---|
| 0 | 后端工程脚手架 / 数据库 schema / 种子数据 | 🟢 后端部分完成（待 frontend 部分） |
| 1 | JWT 鉴权（登录/注册/我的资料） | ⚪ 未开始 |
| 2 | 自习室与座位 + WebSocket 推送 | ⚪ 未开始 |
| 3 | 预约 + 签到 + 定时任务（超时释放） | ⚪ 未开始 |
| 4 | 智能推荐（规则打分） | ⚪ 未开始 |
| 5 | 违规举报 + 站内通知 | ⚪ 未开始 |
| 6 | 巡检 + 公告 + 预约规则管理 | ⚪ 未开始 |
| 7 | 数据统计 + EasyExcel 导出 | ⚪ 未开始 |
| 8 | 操作日志（AOP 切面） | ⚪ 未开始 |
| 9 | 收尾：Knife4j 文档 + README + ER 图 | ⚪ 未开始 |

---

## 已完成内容（按 commit 时间倒序）

```
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

后端**可以编译**（`mvn -DskipTests compile` ✅），但还**不能启动业务流程**——因为还没有任何 `@RestController`。

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

> **MySQL 密码不是 `123456`？** 改 `backend/src/main/resources/application.yml` 里的 `spring.datasource.password`。

---

## 下一步（按优先级）

1. **完成 Phase 0 剩余部分**：前端 Vite + Vue 3 + TS + Pinia + Element Plus 工程骨架 + 双 layout 空壳 + 路由守卫占位 + axios 封装。验证标准：`/student` 与 `/admin` 两条路由各能加载占位页。
2. **Phase 1（鉴权）**：
   - 后端：`AuthController` (注册/登录/getMe)、`AuthService`、登录 DTO
   - 前端：登录页、注册页、Pinia user store、路由守卫挂上 token
   - 验证：用 `stu01 / 123456` 在前端完成登录并刷新页不掉登录态
3. **Phase 2（房间座位 + WS）**：见设计稿
4. ... 依次往后

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
- 还没有任何业务 Controller，所以 Knife4j 页面会是空白的，仅作为 "服务能起" 的探针。
- 前端目录还没动过，连 `package.json` 都没有。
