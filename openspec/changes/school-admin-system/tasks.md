## 1. 项目初始化与环境搭建

- [ ] 1.1 使用 Spring Initializr 创建 Spring Boot 3.x 项目，选择依赖：Spring Web、Spring Security、MyBatis-Plus、MySQL Driver、Lombok、Validation
- [ ] 1.2 配置 `application.yml`：数据源（MySQL 8）、MyBatis-Plus、日志级别
- [ ] 1.3 创建分层目录结构：`controller/`、`service/`、`mapper/`、`entity/`、`dto/`、`security/`、`common/`、`config/`
- [ ] 1.4 添加 Knife4j（Swagger）依赖并完成基础配置，验证 `/doc.html` 可访问
- [ ] 1.5 配置 MyBatis-Plus 分页插件与逻辑删除全局配置（`logic-delete-field: deleted`，`logic-delete-value: 1`，`logic-not-delete-value: 0`）；引入 `mybatis-plus-jsqlparser` 并注册 `PaginationInnerInterceptor`（见 design 决策 13）
- [ ] 1.6 实现 `MetaObjectHandler`，自动填充 `create_time`（INSERT）和 `update_time`（INSERT_UPDATE）
- [ ] 1.7 定义 `BaseEntity`（含 id、createTime、updateTime、deleted），所有 Entity 类继承
- [ ] 1.8 定义 `common/result/PageResult<T>` 和 `common/request/PageRequest`，供所有分页接口复用

## 2. 数据库建表

- [ ] 2.1 编写 `schema.sql`：创建 `sys_user` 表（id、username、password_hash、real_name、role、status、deleted、create_time、update_time）
- [ ] 2.2 编写 `student` 表（id、user_id、class_id、student_no、gender、birth_date、deleted、create_time、update_time）
- [ ] 2.3 编写 `teacher` 表（id、user_id、teacher_no、department、deleted、create_time、update_time）
- [ ] 2.4 编写 `class` 表（id、name、grade、year、deleted、create_time、update_time）
- [ ] 2.5 编写 `course` 表（id、name、code、credit、deleted、create_time、update_time）
- [ ] 2.6 编写 `course_teacher` 中间表（id、course_id、teacher_id、create_time）；`create_time` 由 MetaObjectHandler 自动填充（INSERT），无 `deleted` 软删除字段
- [ ] 2.7 编写 `grade` 表（id、student_id、course_id、score、semester、deleted、create_time、update_time），添加 UNIQUE(student_id, course_id, semester)
- [ ] 2.8 编写 `data.sql`：插入超管默认账号（username: admin，密码 BCrypt 加密）

## 3. 认证与权限（user-auth）

- [ ] 3.1 创建 `UserEntity` 实体类与 `UserMapper`
- [ ] 3.2 实现 `JwtUtil`：生成/解析 Access Token（2h）与 Refresh Token（7d），使用 jjwt 库
- [ ] 3.3 实现 `UserDetailsServiceImpl`，根据 username 加载用户信息
- [ ] 3.4 实现 `JwtAuthenticationFilter`：拦截请求，验证 Token，将认证信息写入 SecurityContext
- [ ] 3.5 配置 `SecurityConfig`：关闭 CSRF、配置白名单（/api/auth/**、/doc.html）、启用方法级注解 `@EnableMethodSecurity`
- [x] 3.6 实现 `AuthController`：`POST /api/auth/login`、`POST /api/auth/refresh`、`POST /api/auth/logout`
- [x] A-4 修复未认证场景 JSON 响应：`JwtAuthenticationFilter` 按异常类型写定制化 401；`GlobalExceptionHandler` 补充 `AuthenticationException` 安全网处理器；`SecurityConfig` 注释更新分工说明
- [ ] 3.7 实现 `AuthService`：登录逻辑（查用户 → 验密 → 颁发 Token）
- [ ] 3.8 创建统一响应体 `Result<T>` 和全局异常处理器 `GlobalExceptionHandler`（处理 AuthenticationException、AccessDeniedException、MethodArgumentNotValidException）
- [ ] 3.9 配置 CORS 全局允许前端域名访问

## 4. 用户管理（user-management）

- [ ] 4.1 创建 `UserController`（路径 `/api/users`），添加 `@PreAuthorize("hasRole('ADMIN')")` 限制
- [ ] 4.2 实现 `GET /api/users`：分页查询用户，支持 role、keyword 过滤
- [ ] 4.3 实现 `POST /api/users`：创建用户，用户名唯一性校验，密码 BCrypt 加密
- [ ] 4.4 实现 `PUT /api/users/{id}`：更新用户信息（realName、role、status），三字段全部必填（全量 PUT，非 PATCH）
- [ ] 4.5 实现 `DELETE /api/users/{id}`：逻辑删除，禁止删除当前登录账号（已知限制：软删除后 username 仍占用 UNIQUE 约束，该用户名永久不可复用；MVP 阶段可接受）
- [ ] 4.6 实现 `PUT /api/users/me/password`：用户修改自己密码（旧密码验证），`@PreAuthorize("isAuthenticated()")` 覆盖 Controller 级 ADMIN 限制

## 5. 班级管理（class-management）

- [ ] 5.1 创建 `ClassEntity`、`ClassMapper`、`ClassService`
- [ ] 5.2 实现 `GET /api/classes`：查询班级列表，含每班学生数（子查询或 LEFT JOIN）
- [ ] 5.3 实现 `POST /api/classes`：创建班级，同年份名称唯一性校验
- [ ] 5.4 实现 `GET /api/classes/{id}`：查询班级详情，含学生列表
- [ ] 5.5 实现 `PUT /api/classes/{id}`：更新班级信息
- [ ] 5.6 实现 `DELETE /api/classes/{id}`：逻辑删除，有学生时拒绝

## 6. 教师管理（teacher-management）

- [ ] 6.1 创建 `TeacherEntity`、`TeacherMapper`、`TeacherService`
- [ ] 6.2 实现 `GET /api/teachers`：分页查询教师，支持 department、keyword 过滤
- [ ] 6.3 实现 `POST /api/teachers`：创建教师档案，工号唯一性校验，关联 user_id
- [ ] 6.4 实现 `GET /api/teachers/{id}`：查询教师详情，含关联课程列表
- [ ] 6.5 实现 `PUT /api/teachers/{id}`：更新教师院系等信息
- [ ] 6.6 实现 `DELETE /api/teachers/{id}`：逻辑删除，有关联课程时拒绝

## 7. 学生管理（student-management）

> **依赖说明**：`classId` 校验仅需 `SchoolClassMapper`（已存在）；E2E 联调造班级数据建议先完成 **5.3**（`POST /api/classes`）或手工插入 `class` 表。骨架类（`Student`、`StudentMapper`、`StudentService`、`StudentController`）已存在，7.0 起为对齐 Spec 与实现业务逻辑。

- [x] 7.0 对齐 Student Entity/DTO 与 Spec：Entity 去 `name`、补 `birthDate`；Create/Update/Response/PageResponse 字段对齐决策 12；`StudentService.page` 增加 `keyword` 参数；Controller `GET /students` 增加 `keyword` 参数
- [x] 7.1 编写 `StudentMapper.xml`：分页列表与详情 JOIN `sys_user`（取 `real_name`）与 `class`（取 `name` 为 `className`）；`keyword` 过滤 `real_name`
- [x] 7.2 实现 `GET /api/students`：分页查询；`classId`、`keyword` 过滤；返回 `realName`、`className`；仅 `ADMIN`/`SUPER_ADMIN`；`@Transactional(readOnly = true)`
- [x] 7.3 实现 `POST /api/students`：HTTP 201；学号唯一（40005「学号已存在」）；`user_id` 存在且 `role=STUDENT`（否则 40004「用户不存在」）；一用户一档案（40005「该用户已有学生档案」）；`class_id` 存在（40004「班级不存在」）；`@Transactional`
- [x] 7.4 实现 `GET /api/students/{id}`：详情含决策 12 字段；管理员/教师可查任意；学生仅查自己（Service 层校验，他人 403）；`@Transactional(readOnly = true)`
- [x] 7.5 实现 `PUT /api/students/{id}`：部分更新 `classId`/`gender`/`birthDate`；至少一个字段；`classId` 存在性校验；`@Transactional`
- [x] 7.6 实现 `DELETE /api/students/{id}`：逻辑删除；有 `grade` 关联则 40006「该学生存在成绩记录，无法删除」；`@Transactional`

## 8. 课程管理（course-management）

- [ ] 8.1 创建 `CourseEntity`、`CourseTeacherEntity`、`CourseMapper`、`CourseTeacherMapper`
- [ ] 8.2 实现 `GET /api/courses`：查询课程列表，含关联教师姓名
- [ ] 8.3 实现 `GET /api/courses/my`：教师查询自己的课程（Spring Security 获取当前用户 teacher_id）
- [ ] 8.4 实现 `POST /api/courses`：创建课程，课程代码唯一性校验
- [ ] 8.5 实现 `PUT /api/courses/{id}`：更新课程信息
- [ ] 8.6 实现 `POST /api/courses/{id}/teachers`：分配课程教师（幂等处理）
- [ ] 8.7 实现 `DELETE /api/courses/{id}/teachers/{teacherId}`：移除课程教师关联，有成绩时拒绝
- [ ] 8.8 实现 `DELETE /api/courses/{id}`：逻辑删除，有成绩时拒绝

## 9. 成绩管理（grade-management）

- [ ] 9.1 创建 `GradeEntity`、`GradeMapper`、`GradeService`
- [ ] 9.2 实现 `POST /api/grades`：教师录入成绩，校验课程归属、分数范围、唯一性约束
- [ ] 9.3 实现 `PUT /api/grades/{id}`：教师修改成绩，校验课程归属权限
- [ ] 9.4 实现 `GET /api/grades/my`：学生查询自己成绩，支持 semester 过滤
- [ ] 9.5 实现 `GET /api/grades`：管理员/教师查询成绩，支持 studentId、courseId、classId、semester 过滤

## 10. 收尾与验证

- [ ] 10.1 为所有 Controller 接口补充 Knife4j 注解（`@Operation`、`@Tag`），验证 `/doc.html` 文档完整
- [ ] 10.2 用 Postman 或 Knife4j 逐一测试认证流程：登录 → 获取 Token → 访问受保护接口 → Token 过期 → 刷新
- [ ] 10.3 验证角色权限边界：学生无法访问管理接口，教师无法操作他人课程成绩
- [ ] 10.4 验证所有业务校验（重复学号、删除有关联数据的记录等）均返回正确错误码
- [ ] 10.5 搭建 Vue 3 + Element Plus 前端基础框架，实现登录页与路由守卫（验证前后端联调）
