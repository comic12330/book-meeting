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
│       │   │                      # 状态码字典（BookStatus/RoomStatus/UserRole）、参数键定义（SysConfigKey）
│       │   ├── config/            # 跨域与拦截器注册、Jackson 时间格式
│       │   ├── interceptor/       # 登录拦截 + 管理端角色校验
│       │   ├── controller/        # 用户端接口
│       │   ├── controller/admin/  # 管理端接口
│       │   ├── service/           # 业务层（冲突校验在 BookService，参数读取在 SysConfigService）
│       │   ├── mapper/            # MyBatis Mapper 接口
│       │   ├── entity/dto/vo/     # 实体、入参、出参
│       │   └── util/              # 令牌、密码摘要
│       └── resources/
│           ├── application.yml    # 部署级参数：数据源、令牌密钥、分页上下限
│           ├── mapper/*.xml       # 手写 SQL
│           └── db/schema.sql      # 建库建表 + 索引 + 初始化数据（含 sys_config 参数表）
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
| GET | `/api/configs` | 可预定窗口等业务参数（登录后可读，前端用于限制日期与人数输入） |

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
| GET | `/api/admin/configs` | 业务参数列表（含类型、取值范围、说明） |
| PUT | `/api/admin/configs` | 修改单个参数，保存即生效 |
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

## 六、业务参数配置

任务书第十一节要求「业务参数不得写死在代码中，会议室容量上限、可选时段、状态码等应通过配置文件或数据表管理」。本项目按参数性质分三层存放，判断标准是「谁需要改它」。

| 层 | 存放位置 | 内容 | 谁能改 |
|---|---|---|---|
| 部署级技术参数 | `application.yml` → `meeting.*` | 数据源、令牌密钥与有效期、分页默认值与上限 | 部署方 / 开发者，改后重启 |
| 业务运营参数 | 数据表 `sys_config` | 单次时长上限、最多提前天数、每日可预定窗口、会议室容量上限 | **管理员在线改，保存即生效，无需重启** |
| 固定业务字典 | Java 常量类 | 预定状态 0–4、会议室状态、角色、密码长度 | 开发者，随代码发布 |

### 6.1 为什么状态码不进配置文件

状态码对应 `status` 列的取值，被 SQL 的 `IN` 条件、审核流程与前端展示共同依赖。改一个值意味着全部历史记录的语义跟着变，它属于「代码与数据共同约定的字典」，不是运营可以随手调整的参数。因此抽成 `BookStatus` / `RoomStatus` / `UserRole` 常量类，作用是消除魔法数字、集中定义；两个「参与冲突判定的状态集合」也放在 `BookStatus` 里，因为它们是状态码的直接派生，分开写容易出现「改了状态码忘改集合」的隐性问题。

### 6.2 sys_config 的管理方式

- 参数项由 `schema.sql` 写入，页面上**只能改值，不能增删键名**——键名是代码里 `SysConfigKey` 枚举声明过的契约，页面新增的键代码读不到。
- 每个参数的类型与取值范围写在 `SysConfigKey` 枚举里（如 `booking.max-hours` 限 1–720），后端保存前校验，管理页的输入控件也从同一份定义取范围，不存在「页面能填、后端拒绝」的口径不一致。
- 读取走内存缓存：首次访问整表加载，管理员保存后直接回写缓存。因此改值**即时生效**，业务校验下一次请求就用新值。若绕过系统直接改库，需打开一次参数页或重启才会同步。
- 跨参数规则单独校验：`booking.daily-start` 必须早于 `booking.daily-end`，两个值分两条记录存，单看一条无法判断。

| 参数键 | 默认值 | 作用 |
|---|---|---|
| `booking.max-hours` | 72 小时 | 单笔预定的起止跨度上限 |
| `booking.max-advance-days` | 30 天 | 最多提前多少天预定，防止长期占位 |
| `booking.daily-start` | 07:00 | 每日最早可预定的开始时刻 |
| `booking.daily-end` | 23:00 | 每日最晚可开始的时刻 |
| `room.max-capacity` | 500 人 | 会议室容纳人数上限 |

「每日窗口」只约束**开始时刻**，结束时刻允许跨到次日——任务书要求支持 20:00 至次日 02:00 这类跨天场景，若把结束也限制在同一天窗口内，跨天就提交不了了。结束时间靠「跨度上限」间接约束。

## 七、核心业务：时段冲突校验

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
7. **后端独立校验**：`checkTimeRange()` 校验结束晚于开始、不早于当前时刻、单次跨度不超上限、开始时刻落在每日可预定窗口内，且不超出最多提前天数；阈值全部读 `sys_config`，不依赖前端时间控件。

## 八、已完成的联调验证

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
| 单次时长超上限（默认 72 小时） | 拦截 |
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
| 参数：每日窗口外开始（06:30 / 23:30） | 均拦截 |
| 参数：跨天 19:00→次日 01:00（在窗口内开始） | 放行 |
| 参数：把时长上限改为 2 小时后提交 4 小时申请 | 立即被拦截，提示「不能超过 2 小时」 |
| 参数：超出最多提前天数（30 天） | 拦截 |
| 参数：把容量上限改为 100 后新增/编辑 150 人会议室 | 均拦截 |
| 参数：非整数 / 超范围 / 非法时间 / 起始晚于最晚开始 / 未知键 | 均返回 400 并说明原因 |
| 参数：pageSize 传 1000 | 按配置上限截断 |

## 九、可直接作为拓展项继续做的功能

任务书列出的 9 项拓展里，以下 5 项与本项目结构契合度最高，加分各 3–5 分：

1. 日历视图展示会议室占用（按 room + 日期查 meeting_book）
2. ECharts 统计各会议室月使用频次与使用率
3. 操作日志（新增 `oper_log` 表 + AOP 切面记录）
4. 定时任务把已结束的预定置为「已完成」（`@Scheduled` + `status=1 AND end_time < now()`）
5. 频繁取消的提交限制（按 user 统计 30 天内取消次数）

## 十、注意事项

- **改了后端代码要重启**：`BookService` / Mapper 的修改不会热生效（工程未装 devtools），IDEA 里停止后重新运行 `MeetingRoomApplication`。
- 密码摘要用 MD5：任务书允许「BCrypt 或 MD5」，选 MD5 是为了建库脚本能离线一次执行。若要升级 BCrypt，只改 `Md5Util` 的 `encode`/`verify` 实现，调用方无需改动（数据库 `password` 字段已是 VARCHAR(64)，足够存 BCrypt 的 60 位）。
- 业务参数分三层管理：技术参数在 `application.yml`，运营参数在 `sys_config` 表（管理员在「系统参数」页在线改，即时生效），固定字典（状态码、角色、密码长度）在 Java 常量类。代码里不再有散落的魔法数字，详见第六节。
- 设备类型（投影仪 / 麦克风 / 视频会议终端）目前定义在前端 `utils/dict.js` 与会议室表的 `equipment` 字段约定中，若要支持管理员自行增减设备类型，需要再加一张设备字典表。
- 删除会议室时若有历史预定记录会被拒绝，引导改为「停用」，避免破坏历史数据。
