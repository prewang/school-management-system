## 1. 项目初始化与环境搭建

- [x] 1.1 使用 Spring Initializr 创建 Spring Boot 3.x 项目，选择依赖：Spring Web、Spring Security、MyBatis-Plus、MySQL Driver、Lombok、Validation
- [x] 1.2 配置 `application.yml`：数据源（MySQL 8）、MyBatis-Plus、日志级别
- [x] 1.3 创建分层目录结构：`controller/`、`service/`、`mapper/`、`entity/`、`dto/`、`security/`、`common/`、`config/`
- [x] 1.4 添加 Knife4j（Swagger）依赖并完成基础配置，验证 `/doc.html` 可访问
- [x] 1.5 配置 MyBatis-Plus 分页插件与逻辑删除全局配置（`logic-delete-field: deleted`，`logic-delete-value: 1`，`logic-not-delete-value: 0`）；引入 `mybatis-plus-jsqlparser` 并注册 `PaginationInnerInterceptor`（见 design 决策 13）
- [x] 1.6 实现 `MetaObjectHandler`，自动填充 `create_time`（INSERT）和 `update_time`（INSERT_UPDATE）
- [x] 1.7 定义 `BaseEntity`（含 id、createTime、updateTime、deleted），所有 Entity 类继承
- [x] 1.8 定义 `common/result/PageResult<T>` 和 `common/request/PageRequest`，供所有分页接口复用

## 2. 数据库建表

- [x] 2.1 编写 `schema.sql`：创建 `sys_user` 表（id、username、password_hash、real_name、role、status、deleted、create_time、update_time）
- [x] 2.2 编写 `student` 表（id、user_id、class_id、student_no、gender、birth_date、deleted、create_time、update_time）
- [x] 2.3 编写 `teacher` 表（id、user_id、teacher_no、department、deleted、create_time、update_time）
- [x] 2.4 编写 `class` 表（id、name、grade、year、deleted、create_time、update_time）
- [x] 2.5 编写 `course` 表（id、name、code、credit、deleted、create_time、update_time）
- [x] 2.6 编写 `course_teacher` 中间表（id、course_id、teacher_id、create_time）；`create_time` 由 MetaObjectHandler 自动填充（INSERT），无 `deleted` 软删除字段
- [x] 2.7 编写 `grade` 表（id、student_id、course_id、score、semester、deleted、create_time、update_time），添加 UNIQUE(student_id, course_id, semester)
- [x] 2.8 编写 `data.sql`：插入超管默认账号（username: admin，密码 BCrypt 加密）

## 3. 认证与权限（user-auth）

- [x] 3.1 创建 `UserEntity` 实体类与 `UserMapper`
- [x] 3.2 实现 `JwtUtil`：生成/解析 Access Token（2h）与 Refresh Token（7d），使用 jjwt 库
- [x] 3.3 实现 `UserDetailsServiceImpl`，根据 username 加载用户信息
- [x] 3.4 实现 `JwtAuthenticationFilter`：拦截请求，验证 Token，将认证信息写入 SecurityContext
- [x] 3.5 配置 `SecurityConfig`：关闭 CSRF、配置白名单（/api/auth/**、/doc.html）、启用方法级注解 `@EnableMethodSecurity`
- [x] 3.6 实现 `AuthController`：`POST /api/auth/login`、`POST /api/auth/refresh`、`POST /api/auth/logout`
- [x] A-4 修复未认证场景 JSON 响应：`JwtAuthenticationFilter` 按异常类型写定制化 401；`GlobalExceptionHandler` 补充 `AuthenticationException` 安全网处理器；`SecurityConfig` 注释更新分工说明
- [x] 3.7 实现 `AuthService`：登录逻辑（查用户 → 验密 → 颁发 Token）
- [x] 3.8 创建统一响应体 `Result<T>` 和全局异常处理器 `GlobalExceptionHandler`（处理 AuthenticationException、AccessDeniedException、MethodArgumentNotValidException）
- [x] 3.9 配置 CORS 全局允许前端域名访问

## 4. 用户管理（user-management）

- [x] 4.1 创建 `UserController`（路径 `/api/users`），添加 `@PreAuthorize("hasRole('ADMIN')")` 限制
- [x] 4.2 实现 `GET /api/users`：分页查询用户，支持 role、keyword 过滤
- [x] 4.3 实现 `POST /api/users`：创建用户，用户名唯一性校验，密码 BCrypt 加密
- [x] 4.4 实现 `PUT /api/users/{id}`：更新用户信息（realName、role、status），三字段全部必填（全量 PUT，非 PATCH）
- [x] 4.5 实现 `DELETE /api/users/{id}`：逻辑删除，禁止删除当前登录账号（已知限制：软删除后 username 仍占用 UNIQUE 约束，该用户名永久不可复用；MVP 阶段可接受）
- [x] 4.6 实现 `PUT /api/users/me/password`：用户修改自己密码（旧密码验证），`@PreAuthorize("isAuthenticated()")` 覆盖 Controller 级 ADMIN 限制

## 5. 班级管理（class-management）

- [x] 5.1 创建 `ClassEntity`、`ClassMapper`、`ClassService`
- [ ] 5.2 实现 `GET /api/classes`：查询班级列表，含每班学生数（子查询或 LEFT JOIN）
- [ ] 5.3 实现 `POST /api/classes`：创建班级，同年份名称唯一性校验
- [ ] 5.4 实现 `GET /api/classes/{id}`：查询班级详情，含学生列表
- [ ] 5.5 实现 `PUT /api/classes/{id}`：更新班级信息
- [ ] 5.6 实现 `DELETE /api/classes/{id}`：逻辑删除，有学生时拒绝

## 6. 教师管理（teacher-management）

> **依赖说明**：创建档案前须先完成 **4.3**（`POST /api/users`，`role=TEACHER`）。`courseNames` 查询与删除校验仅需 `CourseTeacherMapper`（已存在）；E2E 联调造课程关联建议先完成 **8.6**（`POST /api/courses/{id}/teachers`）或手工插入 `course_teacher` 表。骨架类（`Teacher`、`TeacherMapper`、`TeacherService`、`TeacherController`）已存在，6.0 起为对齐 Spec 与决策 14 并实现业务逻辑。

- [x] 6.0 对齐 Teacher Entity/DTO 与 Spec：Entity 去 `name`/`gender`、补 `department`；Create/Update/Response/PageResponse 字段对齐决策 14；`TeacherService.page` 增加 `department`/`keyword` 参数；Controller 路由壳
- [x] 6.1 编写 `TeacherMapper.xml`：分页列表与详情 JOIN `sys_user`（取 `real_name`）；`department` 精确匹配、`keyword` 模糊匹配 `real_name`（不匹配 `username`）；详情 `courseNames` 子查询；`countByTeacherNo`/`countByUserId`（含已软删行）
- [x] 6.2 实现 `GET /api/teachers`：分页查询；`department`、`keyword` 过滤；返回 `realName`；仅 `ADMIN`/`SUPER_ADMIN`；`@Transactional(readOnly = true)`
- [x] 6.3 实现 `POST /api/teachers`：HTTP 201；工号唯一含已删行（40005「工号已存在」）；`user_id` 存在、未删、已启用且 `role=TEACHER`（否则 40004「用户不存在」）；一用户一档案（40005「该用户已有教师档案」）；`@Transactional`
- [x] 6.4 实现 `GET /api/teachers/{id}`：详情含决策 14 字段；`courseNames` 无关联时返回 `[]`；`ADMIN`/`SUPER_ADMIN` 查任意；教师仅查自己（Service 层校验，他人 403）；不存在/已删除 → 40004（不以 403 伪装）；`@Transactional(readOnly = true)`
- [x] 6.5 实现 `PUT /api/teachers/{id}`：部分更新 `department`（必填非 null）；仅 `ADMIN`/`SUPER_ADMIN`；`@Transactional`
- [x] 6.6 实现 `DELETE /api/teachers/{id}`：仅逻辑删除 `teacher` 行，**不级联** `sys_user`；有 `course_teacher` 关联则 40006；软删后工号/`user_id` 不可复用；`@Transactional`
- [x] 6.7 补充 `TeacherServiceImpl` 单元测试：覆盖创建（40004/40005）、列表/更新 403、详情（200/403/40004）、删除（40006、不级联 `sys_user`）；见 spec「模块验收与自动化测试」

## 7. 学生管理（student-management）

> **依赖说明**：`classId` 校验仅需 `SchoolClassMapper`（已存在）；E2E 联调造班级数据建议先完成 **5.3**（`POST /api/classes`）或手工插入 `class` 表。骨架类（`Student`、`StudentMapper`、`StudentService`、`StudentController`）已存在，7.0 起为对齐 Spec 与决策 12 并实现业务逻辑。

- [x] 7.0 对齐 Student Entity/DTO 与 Spec：Entity 去 `name`、补 `birthDate`；Create/Update/Response/PageResponse 字段对齐决策 12；`StudentService.page` 增加 `keyword` 参数；Controller `GET /students` 增加 `keyword` 参数
- [x] 7.1 编写 `StudentMapper.xml`：分页列表与详情 JOIN `sys_user`（取 `real_name`）与 `class`（取 `name` 为 `className`）；`keyword` 过滤 `real_name`
- [x] 7.2 实现 `GET /api/students`：分页查询；`classId`、`keyword` 过滤；返回 `realName`、`className`；仅 `ADMIN`/`SUPER_ADMIN`；`@Transactional(readOnly = true)`
- [x] 7.3 实现 `POST /api/students`：HTTP 201；学号唯一（40005「学号已存在」）；`user_id` 存在且 `role=STUDENT`（否则 40004「用户不存在」）；一用户一档案（40005「该用户已有学生档案」）；`class_id` 存在（40004「班级不存在」）；`@Transactional`
- [x] 7.4 实现 `GET /api/students/{id}`：详情含决策 12 字段；管理员/教师可查任意；学生仅查自己（Service 层校验，他人 403）；`@Transactional(readOnly = true)`
- [x] 7.5 实现 `PUT /api/students/{id}`：部分更新 `classId`/`gender`/`birthDate`；至少一个字段；`classId` 存在性校验；`@Transactional`
- [x] 7.6 实现 `DELETE /api/students/{id}`：逻辑删除；有 `grade` 关联则 40006「该学生存在成绩记录，无法删除」；`@Transactional`

## 8. 课程管理（course-management）

> **依赖说明**：分配教师须有效 `teacher` 档案（§6 已完成）；移除/删除校验仅需 `GradeMapper`（§9.1 已存在），不硬依赖 §9.2–9.5。E2E 验证 40006 可手工插 `grade` 或待 §9.2 联调。骨架类已存在，8.0 起对齐 Spec 与决策 15。

- [x] 8.1 创建 `CourseEntity`、`CourseTeacherEntity`、`CourseMapper`、`CourseTeacherMapper`（仅 Entity/Mapper，不含 Spec 对齐）
- [x] 8.0 对齐 course-management 与 Spec/决策 15：`CourseTeacher` 补 `createTime`；`CoursePageResponse` 补 `teacherNames`；新增 `CourseTeacherAssignRequest`；`CourseService`/Controller 补 `/my`、`/{id}/teachers` 路由；`POST` 加 201；**移除** `GET /courses/{id}`；列表加 `keyword` 与权限注解
- [x] 8.2 实现 `GET /api/courses`：分页；`keyword` 过滤 `course.name`；返回 `teacherNames`（按 `teacher.id` 升序）；仅 `ADMIN`/`SUPER_ADMIN`；`CourseMapper.xml` JOIN 教师姓名；`@Transactional(readOnly = true)`
- [x] 8.3 实现 `GET /api/courses/my`：教师查本人课程；返回 `PageResult<CoursePageResponse>`（含全量 `teacherNames`）；支持 `keyword`；非教师 403；`@Transactional(readOnly = true)`
- [x] 8.4 实现 `POST /api/courses`：HTTP 201；代码唯一含已删行（40005）；`@Transactional`
- [x] 8.5 实现 `PUT /api/courses/{id}`：部分更新 `name`/`credit`；至少一个字段；课程不存在 40004；`@Transactional`
- [x] 8.6 实现 `POST /api/courses/{id}/teachers`：分配教师；幂等；课程/教师不存在 40004；`@Transactional`
- [x] 8.7 实现 `DELETE /api/courses/{id}/teachers/{teacherId}`：移除关联；course 级 grade 存在则 40006；课程/关联不存在 40004；`@Transactional`
- [x] 8.8 实现 `DELETE /api/courses/{id}`：逻辑删除；有 grade 则 40006；课程不存在 40004；`@Transactional`
- [x] 8.9（**optional，不阻塞 MVP**）补充 `CourseServiceImpl` 单元测试：40004/40005/40006、分配幂等、列表/my 权限 403

## 9. 成绩管理（grade-management）

> **依赖说明**：课程归属校验需 `CourseTeacherMapper`、`TeacherMapper`（§6、§8 已完成）；学生「我的成绩」需 `StudentMapper`（§7 已完成）；`classId` 过滤软依赖 `SchoolClassMapper`（§5.1 已存在）。E2E 造数：`POST /api/users` → `POST /api/students` + `POST /api/teachers` + `POST /api/courses` + `POST /api/courses/{id}/teachers`。详见 design 决策 16。

- [x] 9.0 对齐 grade-management 与 Spec/决策 16：移除 `GET /grades/{id}`、`DELETE /grades/{id}`；新增 `GET /grades/my` 路由壳；`POST`/`PUT` 权限改为仅 `TEACHER`；`POST` 加 HTTP 201；`GradeService` 补 `pageMy`、扩展 `page` 参数（`classId`、`semester`）；`GradePageResponse` 补 `courseName`、`studentRealName`；`GradeUpdateRequest.score` 加 `@NotNull`；DTO 分数校验 `message` 对齐「分数须在 0~100 之间」
- [x] 9.1 创建 `GradeEntity`、`GradeMapper`、`GradeService` 骨架（`GradeServiceImpl` 业务逻辑待 §9.3–9.7 实现）
- [x] 9.2 编写 `GradeMapper.xml`：分页 JOIN `grade` + `course` + `student` + `sys_user`；动态过滤 `studentId`/`courseId`/`classId`/`semester`；`countByStudentCourseSemester`；`existsByCourseIdAndTeacherId`（查 `course_teacher`）
- [x] 9.3 实现 `POST /api/grades`：仅 `TEACHER`；校验 `course_teacher` 归属（40301「无权操作该课程成绩」）、`student`/`course` 存在（40004）、分数范围、唯一约束（自定义 400 消息）；`@Transactional`
- [x] 9.4 实现 `PUT /api/grades/{id}`：仅 `TEACHER`；校验成绩存在（40004）、课程归属（40301）；仅更新 `score`；`@Transactional`
- [x] 9.5 实现 `GET /api/grades/my`：仅 `STUDENT`；分页 + 可选 `semester`；解析 `student.user_id`；返回 `courseName`；`@Transactional(readOnly = true)`
- [x] 9.6 实现 `GET /api/grades`：管理员可选 `studentId`/`courseId`/`classId`/`semester`；教师**必须**传 `courseId`（否则 40000）、可选 `semester`、校验归属（40301）；学生 403；`@Transactional(readOnly = true)`
- [x] 9.7（**optional，不阻塞 MVP**）补充 `GradeServiceImpl` 单元测试：录入/修改归属 403、分数越界 400、重复录入 400、学生 `/my` 仅本人、教师缺 `courseId` 40000、管理员 `classId` 过滤

## 10. 收尾与验证

- [ ] 10.1 为所有 Controller 接口补充 Knife4j 注解（`@Operation`、`@Tag`），验证 `/doc.html` 文档完整
- [ ] 10.2 用 Postman 或 Knife4j 逐一测试认证流程：登录 → 获取 Token → 访问受保护接口 → Token 过期 → 刷新
- [ ] 10.3 验证角色权限边界：学生无法访问 `GET /api/grades`（应走 `/my`）；教师无法操作他人课程成绩（录入/修改/查询均 40301「无权操作该课程成绩」）；管理员无法录入/修改成绩（403）；教师无法调用教师列表/创建/更新/删除接口；教师无法查看他人教师详情（403）
- [ ] 10.4 验证所有业务校验均返回正确错误码：重复学号/工号/课程代码（40005）、删除有关联数据（40006，含学生成绩、教师课程关联、课程成绩与移除教师）、禁用用户创建档案（40004「用户不存在」）、成绩录入学生/课程不存在（40004）、重复成绩录入（自定义 400 消息）、分数越界（「分数须在 0~100 之间」）、教师查成绩缺 `courseId`（40000）、教师 `courseNames` 无关联时返回 `[]`、课程列表 `teacherNames` 无关联时返回 `[]`
- [ ] 10.5 搭建 Vue 3 + Element Plus 前端基础框架，实现登录页与路由守卫（验证前后端联调）
