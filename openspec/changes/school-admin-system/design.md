## Context

本项目为全新搭建的学校后台管理系统，没有历史包袱。目标用户为新手开发者，因此技术选型偏向主流、文档丰富、学习曲线平缓的方案。系统为单校单租户，前后端分离架构。

后端：Spring Boot 3.x + Spring Security + MyBatis-Plus + MySQL 8
前端：Vue 3 + Vite + Element Plus（独立仓库或同仓库 frontend/ 子目录）
认证：无状态 JWT（Access Token + Refresh Token）
部署：开发阶段本地运行，后续可打 Docker 镜像

## Goals / Non-Goals

**Goals:**
- 提供完整的 RESTful API，覆盖认证、用户、学生、教师、班级、课程、成绩七个领域
- 基于 Spring Security + JWT 实现无状态认证，角色粒度权限控制
- 数据库设计规范，7 张核心表，关系清晰
- 统一的 API 响应格式与全局异常处理
- Knife4j 自动生成接口文档，方便前端联调

**Non-Goals:**
- 多租户（多所学校）支持
- 排课算法、考勤、财务、消息通知
- 文件上传（头像/附件）、Excel 导入导出
- 移动端 App
- 操作日志审计
- `created_by` / `updated_by` 操作人追踪字段（MVP 阶段不实现；`BaseEntity` 预留扩展位，生产环境可补充）

## Decisions

### 决策 1：ORM 选择 MyBatis-Plus 而非 JPA/Hibernate

**选择**：MyBatis-Plus

**理由**：
- 新手能直接看到 SQL，调试更直观
- MyBatis-Plus 提供 BaseMapper 减少样板代码，同时保留手写 SQL 的灵活性
- 国内资料极为丰富，遇到问题容易搜到解决方案

**放弃 JPA 的原因**：JPA 的 JPQL、懒加载陷阱、N+1 问题对新手不友好

**MyBatis-Plus 使用约定**：
- 条件构建统一使用 `LambdaQueryWrapper`，禁止字段名字符串形式的 `QueryWrapper`（重构改字段名时不报编译错误）
- 单表查询优先用 `BaseMapper` 内置方法；多表 JOIN 或复杂动态 SQL 写 XML，文件放 `resources/mapper/` 目录
- 禁止在 XML/注解 SQL 中使用 `${param}` 拼接，一律用 `#{param}` 占位符，防止 SQL 注入
- 字段自动填充：实现 `MetaObjectHandler`，统一在 INSERT 时填充 `create_time`、在 INSERT_UPDATE 时填充 `update_time`；Entity 字段分别加 `@TableField(fill = FieldFill.INSERT)` 和 `@TableField(fill = FieldFill.INSERT_UPDATE)`，不在业务代码中手动赋值

---

### 决策 2：认证方案使用 JWT（无状态）

**选择**：jjwt 库实现 JWT，Access Token 有效期 2 小时，Refresh Token 7 天

**理由**：
- 前后端分离场景下 JWT 是标准方案，无需 Session 存储
- 便于后续横向扩展（无状态）
- 对新手学习价值高

**风险**：Token 无法主动失效（登出后 Token 在过期前仍有效）
→ 缓解：Access Token 有效期设短（2h），生产环境可引入 Redis 黑名单

---

### 决策 3：角色权限使用 Spring Security 方法级注解

**选择**：`@PreAuthorize("hasRole('ADMIN')")` + 自定义 `UserDetailsService`

**理由**：
- 与 Spring Boot 生态无缝集成
- 注解直接写在 Controller 方法上，权限逻辑可见性高
- 无需引入额外的权限框架（Sa-Token、Shiro）降低复杂度

---

### 决策 4：统一响应格式

所有接口返回统一 JSON 结构：

```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

错误时 `code` 为业务错误码（如 40001 = 用户名不存在），`data` 为 null。
由 `@RestControllerAdvice` 全局处理异常统一封装。

**业务错误码规范**：

> **错误码分两层使用**：
> - **内部码**（40001、40002）：仅用于服务端日志、审计和监控，不对外暴露。
> - **对外响应码**：由 GlobalExceptionHandler 统一映射后返回给客户端，以 spec.md 定义为准。
> - 登录失败（用户名不存在 / 密码错误）对外**统一**返回 40101，消息"用户名或密码错误"，防止用户枚举攻击。

| 错误码 | 场景 | 用途 |
|--------|------|------|
| 40000 | 参数校验失败（MethodArgumentNotValidException） | 对外 |
| 40001 | 用户名不存在（内部判断用） | **仅内部日志** |
| 40002 | 密码错误（内部判断用） | **仅内部日志** |
| 40003 | 账号已禁用 | 对外 |
| 40004 | 资源不存在（通用 Not Found） | 对外 |
| 40005 | 数据重复（用户名 / 学号 / 工号 / 课程代码已存在） | 对外 |
| 40006 | 关联数据存在，禁止删除 | 对外 |
| 40101 | 未登录、Token 已过期、**或登录失败（对外统一）** | 对外 |
| 40301 | 无权限访问（403） | 对外 |
| 50000 | 服务器内部错误 | 对外 |

**自定义异常体系**：在 `common/` 下定义 `BusinessException(int code, String message)` 继承 `RuntimeException`；业务校验失败时直接 `throw new BusinessException(ErrorCode.DATA_DUPLICATE)`；`GlobalExceptionHandler` 捕获后统一封装为标准响应，禁止在 Controller 层手动构造错误码返回。

---

### 决策 5：数据库表设计

7 张核心表，遵循以下约定：
- 所有表使用 `id` BIGINT AUTO_INCREMENT 主键
- 包含 `create_time`、`update_time` 逻辑时间戳字段（DATETIME，由 MetaObjectHandler 自动填充）
- 软删除：`deleted` TINYINT(1) DEFAULT 0（MyBatis-Plus 逻辑删除插件，中间表 `course_teacher` 除外）

**表命名**：`user` 是 MySQL 8 保留字，必须命名为 `sys_user`；Java Entity 对应类名为 `SysUser`，加 `@TableName("sys_user")`。

```
sys_user      ← 所有角色的登录账号（username、password_hash、real_name、role、status）
student       ← 学生档案（student_no、gender、birth_date），关联 sys_user.id（1:1）、class.id（N:1）；**不冗余存储姓名**，展示时 JOIN `sys_user.real_name`
teacher       ← 教师档案（teacher_no、department），关联 sys_user.id（1:1）
class         ← 班级（name、grade、year）
course        ← 课程（name、code、credit）
course_teacher← 课程-教师关联（中间表，course_id、teacher_id、create_time）
grade         ← 成绩（student_id、course_id、score、semester）
```

**关键字段类型与取值规范**：

| 表 | 字段 | 类型 | 取值约定 |
|----|------|------|----------|
| sys_user | role | VARCHAR(20) | SUPER_ADMIN / ADMIN / TEACHER / STUDENT |
| sys_user | status | TINYINT(1) | 0 = 禁用，1 = 启用，默认 1 |
| sys_user | real_name | VARCHAR(100) | 用户真实姓名，NOT NULL |
| student | gender | TINYINT(1) | 0 = 女，1 = 男，2 = 未知 |
| student | birth_date | DATE | 出生日期，可为 NULL |
| teacher | department | VARCHAR(100) | 所属院系，NOT NULL |
| course_teacher | create_time | DATETIME | 关联创建时间，由 MetaObjectHandler 自动填充（INSERT） |
| grade | score | DECIMAL(5,2) | 0.00 ~ 100.00 |
| grade | semester | VARCHAR(10) | 格式 "YYYY-S"，如 "2024-1"（上半年）、"2024-2"（下半年） |

**必要索引**（超出 UNIQUE 约束之外）：

```sql
-- student 按班级查询（高频）
CREATE INDEX idx_student_class_id ON student(class_id);

-- grade 按学生查询（学生查自己成绩）
CREATE INDEX idx_grade_student_id ON grade(student_id);

-- grade 按课程查询（教师查课程成绩）
CREATE INDEX idx_grade_course_id ON grade(course_id);

-- course_teacher 双向查询
CREATE INDEX idx_ct_teacher_id ON course_teacher(teacher_id);
```

---

### 决策 6：项目结构（分层架构）

```
src/main/java/com/school/
  ├── config/          ← Spring Security、Swagger、MyBatis-Plus 配置
  ├── controller/      ← REST Controller，处理 HTTP 请求
  ├── service/         ← 业务逻辑接口（XxxService）+ 实现（impl/XxxServiceImpl）
  ├── mapper/          ← MyBatis-Plus Mapper 接口
  ├── entity/          ← 数据库实体类，均继承 BaseEntity
  ├── dto/             ← 请求 DTO（XxxRequest）/ 响应 DTO（XxxResponse）
  ├── security/        ← JWT 工具类、UserDetailsService、过滤器
  └── common/
        ├── result/    ← Result<T> 统一响应体
        ├── exception/ ← BusinessException、GlobalExceptionHandler
        └── enums/     ← ErrorCode 枚举、Role 枚举等
```

**Service 接口约定**：每个业务模块定义接口（`UserService`）和实现（`impl/UserServiceImpl implements UserService`）。接口只声明方法签名，实现类加 `@Service`；Controller 注入接口而非实现类，便于后续 Mock 测试。

**BaseEntity 约定**：定义公共基类，所有 `entity/` 类继承它，避免在每张表的 Entity 中重复声明公共字段：

```
BaseEntity
  ├── id          (BIGINT, @TableId)
  ├── createTime  (DATETIME, @TableField fill=INSERT)       → 映射列 create_time
  ├── updateTime  (DATETIME, @TableField fill=INSERT_UPDATE) → 映射列 update_time
  └── deleted     (Integer, @TableLogic)                    → 映射列 deleted
```

---

### 决策 7：参数校验使用 Spring Validation

**选择**：Jakarta Bean Validation（`spring-boot-starter-validation`）+ `GlobalExceptionHandler` 统一拦截

**约定**：
- DTO 字段上使用 `@NotBlank`、`@NotNull`、`@Size`、`@Min`/`@Max`、`@Pattern` 等注解声明约束
- Controller 方法参数加 `@Valid` 触发校验
- `GlobalExceptionHandler` 捕获 `MethodArgumentNotValidException`，提取字段错误信息，统一封装为业务错误响应（`code: 40000`，`message` 包含具体字段提示）

**DTO 命名约定**：

| 用途 | 命名规则 | 示例 |
|------|----------|------|
| 创建请求体 | `XxxCreateRequest` | `StudentCreateRequest` |
| 更新请求体 | `XxxUpdateRequest` | `StudentUpdateRequest` |
| 单条响应 | `XxxResponse` | `StudentResponse` |
| 分页列表项 | `XxxPageResponse` | `StudentPageResponse` |
| 登录请求 | `LoginRequest` | — |
| Token 响应 | `TokenResponse` | — |

Entity 类禁止直接暴露给 Controller 层（防止字段过度暴露），所有入参出参均使用对应 DTO。

**理由**：
- 与 Spring Boot 深度集成，无需额外框架
- 校验逻辑集中在 DTO，Controller 保持干净
- 结合全局异常处理，前端可得到统一格式的错误信息，联调体验好

---

### 决策 8：使用 Lombok 减少样板代码

**选择**：Lombok（已含于项目依赖）

**约定**：
- 所有 `entity/` 和 `dto/` 类统一使用 `@Data`（含 getter/setter/equals/hashCode/toString）
- 需要构建器模式的 DTO 加 `@Builder` + `@NoArgsConstructor` + `@AllArgsConstructor`
- Service 实现类使用 `@RequiredArgsConstructor` 替代手写构造注入
- 禁止对 `entity/` 使用 `@EqualsAndHashCode`（避免懒加载陷阱），如需重写需显式声明

**理由**：
- 消除大量重复的 getter/setter 代码，降低新手的认知负担
- 配合 IDE 插件（Lombok Plugin）可正常跳转，不影响可读性

---

### 决策 9：统一分页约定

**选择**：全局默认每页 **10 条**，最大不超过 **100 条**；在 `common/` 下定义可复用的 `PageRequest` 和 `PageResult<T>`

**PageRequest（入参）**：

```
PageRequest
  ├── page   (int, 默认 1，最小 1)
  └── size   (int, 默认 10，最大 100，超出截断)
```

所有分页接口的请求参数继承或组合 `PageRequest`，不再各自重复定义 `page`/`size`。

**PageResult<T>（出参）**：

```
PageResult<T>
  ├── total   (long)   ← 总记录数
  ├── pages   (long)   ← 总页数
  ├── current (long)   ← 当前页
  ├── size    (long)   ← 每页条数
  └── records (List<T>) ← 当前页数据
```

Service 层调用 MyBatis-Plus `Page<T>` 查询后，统一转换为 `PageResult<T>` 再返回给 Controller，屏蔽 MyBatis-Plus 内部结构暴露给前端。

**约定**：
- `PageResult` 放在 `common/result/` 目录，`PageRequest` 放在 `common/request/` 目录
- 所有模块（用户、学生、教师、课程、成绩）的分页接口统一复用，禁止各自重复定义

---

### 决策 10：@Transactional 使用约定

**约定**：
- `@Transactional` 只加在 **Service 实现类的方法**上，禁止加在 Controller 或 Mapper 层
- 只读查询方法加 `@Transactional(readOnly = true)`，减少锁开销
- 涉及多表写操作的方法（如创建用户同时创建学生档案）必须加 `@Transactional`，确保原子性
- 禁止在同一个类中通过 `this.xxx()` 调用带 `@Transactional` 的方法（Spring AOP 代理不生效），需注入自身或拆分到独立 Service

**理由**：事务边界不清晰是新手最常见的 bug 来源，统一约定可避免数据不一致问题。

---

### 决策 11：统一 API 前缀

**选择**：所有后端接口以 `/api` 开头，通过 `application.yml` 中 `server.servlet.context-path=/api` 全局配置，Controller 上的 `@RequestMapping` 不再重复写 `/api`

**各模块路径约定**：

| 模块 | 路径前缀 | 主要接口 |
|------|----------|---------|
| 认证 | `/api/auth` | `POST /api/auth/login`、`POST /api/auth/refresh`、`POST /api/auth/logout` |
| 用户管理 | `/api/users` | `GET /api/users`、`POST /api/users`、`PUT /api/users/{id}`、`DELETE /api/users/{id}`、`PUT /api/users/me/password` |
| 学生管理 | `/api/students` | `GET /api/students`、`POST /api/students`、`GET /api/students/{id}`、`PUT /api/students/{id}`、`DELETE /api/students/{id}` |
| 教师管理 | `/api/teachers` | `GET /api/teachers`、`POST /api/teachers`、`GET /api/teachers/{id}`（详情：管理员查任意，教师仅查自己，见决策 14）、`PUT /api/teachers/{id}`、`DELETE /api/teachers/{id}` |
| 班级管理 | `/api/classes` | `GET /api/classes`、`POST /api/classes`、`GET /api/classes/{id}`、`PUT /api/classes/{id}`、`DELETE /api/classes/{id}` |
| 课程管理 | `/api/courses` | `GET /api/courses`、`GET /api/courses/my`、`POST /api/courses`、`PUT /api/courses/{id}`、`DELETE /api/courses/{id}`、`POST /api/courses/{id}/teachers`、`DELETE /api/courses/{id}/teachers/{teacherId}` |
| 成绩管理 | `/api/grades` | `GET /api/grades`、`GET /api/grades/my`、`POST /api/grades`、`PUT /api/grades/{id}` |

**约定**：
- Controller 类上只写模块路径，如 `@RequestMapping("/students")`，`/api` 由 `context-path` 统一注入
- Spring Security 白名单配置为 `/auth/**`、`/doc.html` 等（不含 `/api` 前缀，因为 Security 在 Servlet 层匹配）
- Knife4j 文档路径不受 `context-path` 影响，仍访问 `/doc.html`
- **HTTP 状态码约定**：所有**创建**接口（POST）返回 HTTP **201**（`@ResponseStatus(HttpStatus.CREATED)`）；查询、更新、删除接口返回 HTTP 200；错误统一由 `GlobalExceptionHandler` 处理

**理由**：集中配置比在每个 Controller 上手写 `/api/xxx` 更安全，前缀变更时只需改一处。

---

### 决策 12：学生管理模块约定

**学生姓名来源**：
- `student` 表不存储姓名字段；列表与详情中的 `realName` 通过 JOIN `sys_user.real_name` 获取。
- 列表 `keyword` 参数对 `sys_user.real_name` 做模糊匹配，不匹配 `username`。

**创建校验**：
- `user_id` 须存在、未软删除、且 `role = STUDENT`；否则统一对外返回 40004「用户不存在」（防止用户枚举）。
- `class_id` 须对应未删除的班级；否则返回 40004「班级不存在」。
- 一用户一档案：重复创建返回 40005「该用户已有学生档案」。

**更新语义**：
- `PUT /api/students/{id}` 采用**部分更新**（仅更新请求体中非 null 字段），与 `PUT /api/users/{id}` 的全量 PUT 语义**刻意区分**。
- 可更新字段：`classId`、`gender`、`birthDate`；至少传入一个，否则 40000。

**删除约束**：
- 删除学生前检查 `grade` 表是否存在关联记录；有则返回 40006「该学生存在成绩记录，无法删除」（与课程/教师删除约束一致）。

**跨模块依赖**：
- 实现 `classId` 校验仅需 `SchoolClassMapper.selectById`（班级 Entity/Mapper 已存在），**不硬依赖** class-management 5.2–5.6 完成。
- E2E 联调造班级测试数据时，建议先完成 class-management 5.3（`POST /api/classes`）或手工插入 `class` 表。

**响应字段**：
- `StudentResponse`：`id, userId, studentNo, realName, gender, birthDate, classId, className, createTime`
- `StudentPageResponse`：`id, studentNo, realName, gender, classId, className`

---

### 决策 14：教师管理模块约定

**权限范围**：
- 列表、创建、更新、删除接口：仅 `ADMIN` / `SUPER_ADMIN`（Controller 层 `@PreAuthorize`），与 user-management、student-management 一致。
- 详情接口：Controller 层不设角色限制；`ADMIN` / `SUPER_ADMIN` 可查任意教师，教师仅可查自己（Service 层校验 `teacher.user_id` 与当前用户 ID，他人返回 403）。
- 详情存在性判定：**资源不存在或已删除** → 40004「资源不存在」；**资源存在但非本人**（仅教师角色）→ 403。不以 403 伪装资源不存在（防止 ID 枚举）。

**教师姓名来源**：
- `teacher` 表不存储姓名字段；列表与详情中的 `realName` 通过 JOIN `sys_user.real_name` 获取。
- 列表 `keyword` 参数对 `sys_user.real_name` 做模糊匹配，不匹配 `username`。

**创建校验**：
- `user_id` 须存在、未软删除、已启用（`status = 1`）、且 `role = TEACHER`；否则统一对外返回 40004「用户不存在」（防止用户枚举）。
- 工号全局唯一：重复（含已软删除档案占用的工号）返回 40005「工号已存在」。
- 一用户一档案：重复创建返回 40005「该用户已有教师档案」。

**软删除与唯一约束**：
- `teacher_no`、`user_id` 的数据库 UNIQUE 约束不区分 `deleted` 状态；MVP 阶段软删除后工号与用户均不可复用（`countByTeacherNo` / `countByUserId` 统计含已删行），与 user-management 用户名策略一致。

**更新语义**：
- `PUT /api/teachers/{id}` 采用**部分更新**（当前 MVP 仅 `department` 一个可更新字段），与 `PUT /api/users/{id}` 的全量 PUT 语义刻意区分。
- `department` 须为非 null；教师角色调用本接口返回 403。

**删除约束**：
- 删除教师前检查 `course_teacher` 表是否存在关联记录；有则返回 40006「该教师仍有关联课程，请先移除课程关联」。
- 仅逻辑删除 `teacher` 表记录，**不级联**软删除关联的 `sys_user`；账号注销由 user-management `DELETE /api/users/{id}` 单独处理（与学生档案删除策略一致）。

**验收与测试**：
- MVP 阶段须满足 `tasks.md` §10.3、§10.4 教师模块手工验收项。
- **建议**补充 `TeacherServiceImpl` 单元测试（Mockito 或 `@SpringBootTest`），覆盖：创建校验（40004/40005）、列表/更新非管理员 403、详情权限（200/403/40004）、删除关联校验（40006）及不级联 `sys_user`。

**跨模块依赖**：
- 详情 `courseNames` 与删除校验仅需 `course` / `course_teacher` 表及对应 Mapper（已存在），**不硬依赖** course-management 8.2–8.8 完成。
- E2E 联调造课程关联数据时，建议先完成 course-management 8.6（`POST /api/courses/{id}/teachers`）或手工插入 `course_teacher` 表。
- 创建教师档案前须先通过 user-management 创建 `role = TEACHER` 的账号（`POST /api/users`）。

**响应字段**：
- `TeacherResponse`：`id, userId, teacherNo, realName, department, courseNames, createTime`（`courseNames` 无关联时返回 `[]`，不省略字段）
- `TeacherPageResponse`：`id, teacherNo, realName, department`

---

### 决策 15：课程管理模块约定

**权限范围**：
- 列表 `GET /api/courses`：仅 `ADMIN` / `SUPER_ADMIN`；支持分页与 `keyword`（匹配 `course.name`）。
- 教师我的课程 `GET /api/courses/my`：仅 `TEACHER`；同样支持 `keyword`。
- 创建/更新/删除/分配/移除：仅 `ADMIN` / `SUPER_ADMIN`。
- MVP **不提供** `GET /api/courses/{id}` 详情接口。

**响应字段**：
- `CoursePageResponse`：`id, name, code, credit, teacherNames`（`List<String>`，无关联时 `[]`）。
- `CourseResponse`：`id, name, code, credit, createTime`（创建/更新返回用；不含 teacherNames）。
- `GET /api/courses/my` 复用 `PageResult<CoursePageResponse>`，列表项字段与 `GET /api/courses` 一致；`teacherNames` 为该课程**全量**关联教师姓名（非仅当前教师）。

**teacherNames 排序**：
- 按 `teacher.id` 升序稳定排列（与 `TeacherMapper.selectCourseNamesByTeacherId` 按 `course.id` 排序的惯例对称）。

**资源不存在（40004）**：
- 课程不存在或已软删除：更新、删除、分配教师时 → 40004「资源不存在」。
- 移除教师时：课程不存在、已软删除，或 course_teacher 关联不存在 → 40004「资源不存在」。
- 与 teacher/student 模块一致，不以 403 伪装资源不存在（防止 ID 枚举）。

**创建校验**：
- 课程代码全局唯一；`countByCode` 统计含已软删行（40005「课程代码已存在」）。

**更新语义**：
- `PUT /api/courses/{id}` 部分更新，仅 `name`/`credit`；至少一个非 null；`code` 不可改。

**删除与关联**：
- 删除课程：检查 `grade` 按 `course_id`；有则 40006「该课程已有成绩数据，无法删除」；仅逻辑删除 `course`，不级联删除 `course_teacher`。
- 移除教师：同样按 `course_id` 检查 `grade`；有则 40006，消息「该课程已有成绩数据，无法移除教师关联」。
- 分配教师：幂等；课程或教师不存在/已软删 → 40004「资源不存在」。

**中间表**：
- `CourseTeacher` 含 `createTime`，`@TableField(fill = INSERT)`；不继承 `BaseEntity`，不做软删除。

**跨模块依赖**：
- 分配教师需有效 `teacher` 档案（teacher-management）；成绩检查仅需 `GradeMapper`，不硬依赖 grade-management API 完成。
- E2E 造数：先 `POST /api/courses` + `POST /api/courses/{id}/teachers` 或手工插表。
- E2E 验证移除/删除 40006：可手工向 `grade` 表插入测试数据，或待 grade-management §9.3 完成后联调。

**验收与测试**：
- MVP 须满足 tasks.md §8 手工验收项；**8.9 单元测试 optional，不阻塞 MVP 合并**；建议 `CourseServiceImpl` 单元测试（见 tasks 8.9）。

---

### 决策 16：成绩管理模块约定

**接口范围（MVP）**：
- 提供：`GET /api/grades`、`GET /api/grades/my`、`POST /api/grades`、`PUT /api/grades/{id}`。
- **不提供**：`GET /api/grades/{id}` 详情、`DELETE /api/grades/{id}` 删除（MVP 无需求，后续如需再开 Change）。

**权限范围**：
- `POST /api/grades`、`PUT /api/grades/{id}`：仅 `TEACHER`（Controller `@PreAuthorize`）；管理员不得代为录入/修改成绩。
- `GET /api/grades/my`：仅 `STUDENT`。
- `GET /api/grades`：`ADMIN` / `SUPER_ADMIN` 可查任意；`TEACHER` 仅可查本人负责课程（须传 `courseId`）；`STUDENT` 返回 403。

**课程归属校验**：
- 教师录入/修改/按课程查询时，须在 Service 层校验 `course_teacher` 关联（`teacher_id` = 当前教师档案 ID），**不能只依赖角色判断**（见 Risks「成绩录入权限边界」）。
- 无权操作课程成绩时，抛出 `BusinessException(ErrorCode.FORBIDDEN, "无权操作该课程成绩")`（HTTP 403）；**禁止**使用 `AccessDeniedException`（`GlobalExceptionHandler` 会吞掉自定义消息）。

**创建校验**：
- `studentId` 对应未删除的 `student` 档案；`courseId` 对应未删除的 `course`；否则 40004「资源不存在」。
- 分数范围 0.00~100.00（最多两位小数，与 `DECIMAL(5,2)` 一致）；越界消息「分数须在 0~100 之间」（DTO `@DecimalMin`/`@DecimalMax` 的 `message` 与之对齐）。
- 唯一约束 `(student_id, course_id, semester)` 冲突 → 400，消息「该学生本学期此课程成绩已存在，请使用修改接口」（`BusinessException` 自定义消息，非 `DATA_DUPLICATE` 默认文案）。

**更新语义**：
- `PUT /api/grades/{id}` 仅更新 `score`；`score` 必填非 null。
- 成绩不存在或已软删除 → 40004「资源不存在」。

**查询过滤**：
- 全局分页：所有列表接口复用 `PageRequest` / `PageResult`（含 `GET /api/grades/my`）。
- 管理员 `GET /api/grades`：可选 `studentId`、`courseId`、`classId`、`semester`。
- 教师 `GET /api/grades`：**必须**传 `courseId`；可选 `semester`；**不得**使用 `classId` 或 `studentId` 参数；未传 `courseId` → 40000「courseId 不能为空」。

**姓名与课程名来源**：
- 列表 JOIN `course.name`（`courseName`）、`sys_user.real_name`（`studentRealName`）；`student` 表不冗余姓名。

**响应字段**：
- `GradeResponse`（创建/更新返回）：`id, studentId, courseId, score, semester, createTime`。
- `GradePageResponse`（列表项）：`id, studentId, studentRealName, courseId, courseName, score, semester`。

**跨模块依赖**：
- 课程归属校验需 `CourseTeacherMapper`、`TeacherMapper`（解析当前用户 → `teacher.id`）。
- 学生「我的成绩」需 `StudentMapper`（`user_id` → `student.id`）。
- `classId` 过滤仅需 `student.class_id` JOIN，软依赖 `SchoolClassMapper`（§5.1 已存在），不硬依赖 class-management 5.2–5.6。
- E2E 造数：`POST /api/users` → `POST /api/students` + `POST /api/teachers` + `POST /api/courses` + `POST /api/courses/{id}/teachers` → 再测成绩接口。

**验收与测试**：
- MVP 须满足 `tasks.md` §9 与 §10.3、§10.4 成绩相关验收项。
- **9.7 单元测试 optional，不阻塞 MVP 合并**；建议 `GradeServiceImpl` 单元测试覆盖录入/修改归属 403、分数越界、重复录入、`/my` 仅本人、`GET /grades` 角色过滤。

---

### 决策 13：MyBatis-Plus 3.5.9 分页依赖

自 MyBatis-Plus 3.5.9 起，分页插件拆分为独立模块，项目须同时引入：
- `mybatis-plus-spring-boot3-starter`
- `mybatis-plus-jsqlparser`（版本与 starter 一致）

`MyBatisPlusConfig` 中须注册 `PaginationInnerInterceptor(DbType.MYSQL)`，否则自定义 XML 分页查询不会追加 `LIMIT`。

## Risks / Trade-offs

- **JWT 无法即时失效** → Access Token 设为 2h 短期，可接受；生产环境补充 Redis 黑名单
- **Refresh Token 7 天窗口** → Refresh Token 客户端存储（localStorage/Cookie），无服务端存储，泄露后 7 天内无法失效；MVP 可接受，生产环境应持久化 Refresh Token 到 DB 并支持主动吊销
- **登录接口无频率限制** → 存在暴力破解风险；MVP 阶段依赖 BCrypt 的慢哈希缓解，生产环境应在网关层或 Spring Security 层加入登录失败次数限制（如连续失败 5 次锁定 15 分钟）
- **MyBatis-Plus 逻辑删除** 与关联查询需注意过滤条件，中间表（course_teacher）不做软删除
- **前后端跨域（CORS）** → Spring Security 配置 `CorsConfigurationSource` 全局处理，禁止 `allowedOrigins("*")` 通配符；明确指定允许的前端域名
- **密码存储** → 必须使用 BCryptPasswordEncoder，绝不明文存储；`data.sql` 中的超管默认密码须在首次登录后强制修改
- **成绩录入权限边界** → 教师只能操作自己课程的成绩，需在 Service 层校验 course_teacher 关联，不能只依赖角色判断
- **Knife4j 生产环境暴露** → 默认 `/doc.html` 无鉴权，生产部署时必须通过 Spring Profile（`spring.profiles.active=prod`）或配置 `knife4j.enable=false` 关闭，防止接口信息泄露

## Migration Plan

全新项目，无迁移需求。首次启动流程：
1. 执行 `schema.sql` 建表
2. 执行 `data.sql` 插入超管默认账号
3. 启动 Spring Boot 应用
4. 通过 Knife4j UI（`/doc.html`）验证接口可用

## Open Questions

- 前端是否与后端同仓库（mono-repo）还是独立仓库？（建议新手放同一仓库，`frontend/` 子目录）
- ~~是否需要分页查询的默认页大小约定？~~ → 已解决，见决策 9：默认 10 条/页，上限 100 条
- ~~成绩是否允许同一学生同一课程多次录入（重修场景）？~~ → 已解决：MVP 阶段 `grade` 表加 `UNIQUE(student_id, course_id, semester)` 约束，重修场景后续可扩展
- ~~学生姓名存储与 keyword 匹配字段？~~ → 已解决，见决策 12：JOIN `sys_user.real_name`，student 表不存 name
- ~~删除学生是否检查成绩关联？~~ → 已解决，见决策 12：有成绩则 40006 拒绝删除
- ~~学生 PUT 全量还是部分更新？~~ → 已解决，见决策 12：部分更新，与 user-management 全量 PUT 区分
- ~~教师管理权限、keyword 匹配、软删除复用、courseNames 空值语义？~~ → 已解决，见决策 14
- ~~删除教师是否级联 sys_user？详情 403 vs 40004？模块测试 scope？~~ → 已解决，见决策 14（删除不级联、详情存在性判定、验收与测试）
- ~~课程模块权限、更新语义、软删代码复用、移除教师成绩检查？~~ → 已解决，见决策 15
- ~~课程资源不存在错误码、/my 响应 DTO、teacherNames 排序、8.9 是否阻塞 MVP、E2E grade 造数？~~ → 已解决，见决策 15（40004 统一、复用 CoursePageResponse、teacher.id 升序、8.9 optional、grade 手工插或等 §9.3）
- ~~成绩模块权限边界、分页、小数位、classId 过滤、403 文案、是否提供删除接口？~~ → 已解决，见决策 16（仅教师录入/修改、/my 与列表均分页、最多两位小数、classId 仅管理员、BusinessException 自定义 403 消息、MVP 无 DELETE/GET-by-id）
