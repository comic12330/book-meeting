# 基于 Spring Boot 的会议室预定系统

综合项目课程实践任务书对应实现。前后端分离：后端 Spring Boot + 原生 MyBatis + MySQL 8.0，前端 Vue 3 + Element Plus。

## 一、目录结构

```
会议预定系统/
├── backend/                        # 后端 Maven 工程（IDEA 直接打开此目录）
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/example/meetingroom/
│       │   ├── MeetingRoomApplication.java
│       │   ├── common/            # 统一返回体、分页、业务异常、全局异常、登录上下文
│       │   ├── config/            # 跨域与拦截器注册、Jackson 时间格式
│       │   ├── interceptor/       # 登录拦截 + 管理端角色校验
│       │   ├── controller/        # 用户端接口
│       │   ├── controller/admin/  # 管理端接口
│       │   ├── service/           # 业务层（冲突校验在 BookService）
│       │   ├── mapper/            # MyBatis Mapper 接口
│       │   ├── entity/dto/vo/     # 实体、入参、出参
│       │   └── util/              # 令牌、密码摘要
│       └── resources/
│           ├── application.yml
│           ├── mapper/*.xml       # 手写 SQL
│           └── db/schema.sql      # 建库建表 + 索引 + 初始化数据
└── frontend/                       # 前端 Vite 工程（VS Code 直接打开此目录）
    ├── package.json
    ├── vite.config.js              # /api 代理到 8080
    └── src/
        ├── api/                    # axios 实例与接口封装
        ├── router/                 # 路由 + 登录/角色守卫
        ├── layout/                 # 主框架（侧边菜单 + 顶栏）
        ├── utils/                  # 令牌存储、数据字典
        └── views/                  # 登录、注册、首页、预定、我的预定、资料、管理端
```

## 二、环境要求

| 组件 | 版本 | 说明 |
|---|---|---|
| JDK | 11（或 8） | 本机 `D:\My Java\jdk 11`，Spring Boot 2.7.18 支持 Java 8–19 |
| Maven | 3.6+ | 本机 `D:\maven\apache-maven-3.6.1` |
| MySQL | 8.0 | 本机已装 8.0，字符集 utf8mb4 |
| Node.js | 18+ | 本机 v22 |

## 三、启动步骤

### 1. 初始化数据库

```bash
mysql -uroot -p < backend/src/main/resources/db/schema.sql
```

或用 Navicat 打开 `backend/src/main/resources/db/schema.sql` 整体执行。脚本会建库 `meeting_room`、建四张表、建索引与外键，并写入初始化数据。

### 2. 改数据库账号

打开 `backend/src/main/resources/application.yml`，把 `username` / `password` 改成你本机 MySQL 的账号密码（默认写的是 `root` / `123456`）。

### 3. 启动后端

IDEA 打开 `backend` 目录，等依赖下载完，运行 `MeetingRoomApplication`。
或命令行：

```bash
cd backend
mvn spring-boot:run
```

启动后访问 `http://localhost:8080/druid` 可看 SQL 监控（账号 admin / admin）。

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
```

浏览器打开 `http://localhost:5173`。

### 5. 默认账号

| 账号 | 密码 | 角色 |
|---|---|---|
| admin | 123456 | 管理员 |
| zhangsan | 123456 | 普通用户 |
| lisi | 123456 | 普通用户 |

## 四、接口清单

统一返回体：`{ "code": 200, "msg": "操作成功", "data": ... }`
除登录、注册外，所有接口需在请求头带 `Authorization: Bearer <token>`。

### 公共

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/api/auth/login` | 登录，返回 token |
| POST | `/api/auth/register` | 注册 |
| GET | `/api/auth/me` | 当前登录用户资料 |
| GET | `/api/notices` | 首页公告（is_show=1） |

### 用户端

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/rooms` | 会议室分页查询（仅启用），支持名称/位置/最少容纳/设备筛选 |
| GET | `/api/rooms/{id}` | 会议室详情 |
| POST | `/api/books` | 提交预定（含冲突校验） |
| GET | `/api/books/my` | 我的预定分页 |
| GET | `/api/books/{id}` | 预定详情（越权访问返回 403） |
| PUT | `/api/books/{id}/cancel` | 取消未开始的预定 |
| GET | `/api/user/profile` | 个人资料 |
| PUT | `/api/user/profile` | 修改资料 |
| PUT | `/api/user/password` | 修改密码 |

### 管理端（role=1）

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/admin/rooms` | 会议室分页（含停用） |
| POST | `/api/admin/rooms` | 新增会议室 |
| PUT | `/api/admin/rooms/{id}` | 编辑会议室 |
| DELETE | `/api/admin/rooms/{id}` | 删除会议室（有预定记录时拒绝） |
| GET | `/api/admin/books` | 预定分页，支持状态/会议室/关键字筛选 |
| PUT | `/api/admin/books/{id}/audit` | 审核（status=1 通过，2 驳回，驳回必填理由） |
| GET | `/api/admin/users` | 用户分页，支持账号/姓名模糊、角色/状态筛选 |
| POST | `/api/admin/users` | 新增用户（账号、初始密码必填） |
| PUT | `/api/admin/users/{id}` | 编辑用户（只能改姓名、电话、角色、状态，账号不可变） |
| PUT | `/api/admin/users/{id}/status/{status}` | 启用 / 停用（0 停用，1 启用） |
| PUT | `/api/admin/users/{id}/password` | 重置密码（不校验原密码） |
| GET/POST/PUT/DELETE | `/api/admin/notices` | 公告维护 |

## 五、用户管理模块

管理端「用户管理」承担任务书基础模块里的**用户管理**职责（必做项「用户模块」只要求注册/登录/资料/角色，用户管理属课程学时分配表中单列的一块）。

### 5.1 权限模型

- 所有 `/api/admin/**` 由 `AuthInterceptor` 统一校验 `role = 1`，接口内不再重复判断，避免漏判。
- **令牌不回传账号状态**：令牌只携带 `userId|username|role` 与过期时间。若拦截器只验签名，管理员停用或降级某人后，对方手上的旧令牌仍能用满整个有效期，"停用"就只对下一次登录生效。
  因此 `AuthInterceptor` 在验签之后**回查一次数据库**，以库中的 `status`、`role` 为准重建登录上下文：
  - 账号被停用 → 401「账号已被停用，请联系管理员」，旧令牌**立即失效**；
  - 角色被降级 → 后续管理端请求 403，旧令牌**立即失去管理权限**。
  代价是每个请求多一次主键查询，对课设量级可接受；若要优化可加短 TTL 缓存。

### 5.2 业务约束

| 约束 | 原因 |
|---|---|
| 不能停用当前登录的账号 | 避免管理员误操作把自己锁在系统外 |
| 唯一启用管理员不能降级为普通用户 | 降级是让系统失去管理入口的唯一路径（停用自己已被上一条拦住） |
| 账号不可修改、只可停用不可删除 | `meeting_book.user_id` 外键引用用户，物理删除会破坏历史预定记录 |
| 重置密码不校验原密码 | 执行者身份已由管理员权限校验；管理员本就看不到他人原密码 |

### 5.3 重置密码与「密码找回」的关系

两者不冲突：密码找回是**未登录用户证明"我是我"**（验证码/密保），管理员重置是**已登录管理员证明"我有权限"**，最终都落到同一个 `updatePassword(userId, newPassword)`。管理员重置按"管理员指定新密码"实现，不设固定默认值——避免出现"所有人重置后密码都是 123456"的漏洞，也省掉一个 `need_change_pwd` 标记字段。

## 六、核心业务：时段冲突校验

判定条件（`MeetingBookMapper.xml` → `countConflict`）：

```sql
SELECT COUNT(1) FROM meeting_book
WHERE room_id = #{roomId}
  AND status IN (0, 1)              -- 提交环节：待审核、已通过 都算占用
  AND start_time < #{endTime}       -- 已有记录开始 早于 新预定结束
  AND end_time   > #{startTime}     -- 已有记录结束 晚于 新预定开始
```

设计要点：

1. **两次校验**：`BookService.submit()` 提交时校验一次；`BookService.audit()` 审核通过时再校验一次（排除自身 id）。因为两条待审核记录可能时段重叠，先通过的那条占位后，后一条若不再校验就会被放进系统。
2. **两次校验的"有效状态"不同**：提交时 `status IN (0,1)`（待审核也算占用，防止同一时段被反复提交）；审核时只算 `status = 1`（已通过）。若审核时也把待审核算进来，两条重叠的待审记录会互相阻断、谁都无法通过——这是本项目踩过并修掉的一个坑。
3. **审核串行化**：审核通过前先 `SELECT id FROM meeting_room WHERE id = ? FOR UPDATE` 锁住会议室行，把同一会议室的审核串行执行，避免两个管理员同时点「通过」各自校验都通过、结果双双入库。锁随 `@Transactional` 在方法结束时释放。
4. **判定交给数据库**：用 `COUNT` 一次算完，不在 Java 里先查再比，缩小并发窗口。
5. **索引**：`meeting_book` 上建 `idx_book_room_start (room_id, start_time)`，冲突查询走该索引。
6. **跨天支持**：起止时间都是 `datetime`，用 `LocalDateTime` 比较，20:00–次日 02:00 可正常判定。
7. **后端独立校验**：`checkTimeRange()` 校验结束晚于开始、不早于当前时间、单次不超过 72 小时，不依赖前端时间控件。

## 七、已完成的联调验证

在 MySQL 8.0 + JDK 11 环境实跑，接口层逐条验证（非纸面推演）：

| 场景 | 结果 |
|---|---|
| 与已通过预定重叠（room1 09:00~10:30 已占，提交 09:30~11:00） | 拦截 |
| 包含关系（提交 09:15~10:45） | 拦截 |
| 跨天冲突（room3 19:00~次日 01:00 已占，提交次日 00:00~02:00） | 拦截 |
| 紧邻不重叠（10:30~11:30） | 放行 |
| 不同会议室同时段 | 放行 |
| 开始晚于结束 / 开始等于结束 | 拦截 |
| 参会人数超会议室容量 | 拦截 |
| 单次时长超 72 小时 | 拦截 |
| 预定已停用会议室 | 拦截 |
| 匿名 / 伪造 token 提交 | 401 |
| 普通用户访问 `/api/admin/**` | 403 |
| 审核两条重叠的待审记录：先通过 A，再通过 B | A 通过，B 被拦截 |
| 驳回后同时段重新提交（与原已通过记录重叠） | 拦截 |
| 删除有预定记录的会议室 | 拒绝并提示改为停用 |
| 重复用户名注册 / 原密码错误的改密请求 | 拦截 |
| 非本人取消他人预定 | 403 |
| 中文请求体、分页边界、公告增删改 | 正常 |
| 用户管理：新增重名账号 / 账号格式 / 密码长度 / 手机号格式 | 均拦截 |
| 用户管理：编辑时提交不同 username | 账号保持不变（服务端只更新姓名、电话、角色、状态） |
| 用户管理：停用自己（含编辑里把状态改为停用） | 拒绝 |
| 用户管理：唯一管理员降级为普通用户 | 拒绝 |
| 用户管理：管理员重置他人密码 → 新密码可登录、旧密码失效 | 通过 |
| 用户管理：停用账号后旧令牌再请求 | 401，立即失效 |
| 用户管理：管理员被降级后旧令牌访问管理接口 | 403，权限立即收回 |
| 用户管理：普通用户访问 `/api/admin/users`、伪造令牌 | 403 / 401 |

## 八、可直接作为拓展项继续做的功能

任务书列出的 9 项拓展里，以下 5 项与本项目结构契合度最高，加分各 3–5 分：

1. 日历视图展示会议室占用（按 room + 日期查 meeting_book）
2. ECharts 统计各会议室月使用频次与使用率
3. 操作日志（新增 `oper_log` 表 + AOP 切面记录）
4. 定时任务把已结束的预定置为「已完成」（`@Scheduled` + `status=1 AND end_time < now()`）
5. 频繁取消的提交限制（按 user 统计 30 天内取消次数）

## 九、注意事项

- **改了后端代码要重启**：`BookService` / Mapper 的修改不会热生效（工程未装 devtools），IDEA 里停止后重新运行 `MeetingRoomApplication`。
- 密码摘要用 MD5：任务书允许「BCrypt 或 MD5」，选 MD5 是为了建库脚本能离线一次执行。若要升级 BCrypt，只改 `Md5Util` 的 `encode`/`verify` 实现，调用方无需改动（数据库 `password` 字段已是 VARCHAR(64)，足够存 BCrypt 的 60 位）。
- 业务参数（时长上限、设备枚举、状态码）目前 `MAX_HOURS` 在 `BookService` 以常量定义，若要满足「业务参数不写死」的评分要求，可迁到 `application.yml` 或字典表。
- 删除会议室时若有历史预定记录会被拒绝，引导改为「停用」，避免破坏历史数据。
