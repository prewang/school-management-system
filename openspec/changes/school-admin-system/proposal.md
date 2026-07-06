## Why

学校缺乏统一的数字化后台管理平台，教务数据（学生档案、课程、成绩）分散在表格或纸质记录中，管理效率低下且易出错。本系统以 MVP 形式提供一个基于 Spring Boot 的 Web 后台，让管理员、教师、学生能在同一平台上管理和查阅教务数据。

## What Changes

- 新建 Spring Boot 后端项目，提供 RESTful API
- 实现基于 JWT 的登录认证与角色权限控制（超管 / 管理员 / 教师 / 学生）
- 提供学生档案、教师档案、班级、课程的完整 CRUD 管理；成绩模块提供录入/修改/查询（MVP 无成绩删除与按 ID 详情接口，见 `grade-management` spec 与 design 决策 16）
- 教师档案：管理员维护工号与院系，教师可查看本人档案及关联课程；删除前校验课程关联（见 `teacher-management` spec 与 design 决策 14）
- 教师可录入并修改自己课程的学生成绩
- 学生只能查看自己的成绩与课程信息
- 前端使用 Vue 3 + Element Plus 提供操作界面

## Capabilities

### New Capabilities

- `user-auth`: 用户登录、JWT 令牌颁发与刷新、登出，以及基于角色的访问控制（RBAC）
- `user-management`: 用户账号的增删改查，含角色分配，由管理员操作
- `student-management`: 学生档案（基本信息、所属班级）的增删改查
- `teacher-management`: 教师档案（工号、所属院系）的增删改查；姓名 JOIN `sys_user.real_name`；管理员（ADMIN / SUPER_ADMIN）全权维护，教师仅可查看本人详情（含 `courseNames`）；创建依赖 user-management 先建 TEACHER 账号；软删除后工号不可复用；实现约定见 design 决策 14
- `class-management`: 班级的增删改查，班级与学生的关联管理
- `course-management`: 课程 CRUD 与教师分配；管理员查全部列表、教师查 `/my`；MVP 无课程详情接口；实现约定见 design 决策 15
- `grade-management`: 成绩录入与修改（仅教师，管理员不得代操作）；学生分页查 `/my`；管理员分页查全部（含 `classId` 过滤）；教师分页查本人负责课程（须 `courseId`）；MVP 无 `DELETE` 与 `GET /{id}`；实现约定见 design 决策 16

### Modified Capabilities

## Impact

- 全新项目，无已有代码受影响
- 依赖：Spring Boot 3.x、Spring Security、MyBatis-Plus、MySQL 8、JWT（jjwt）
- 前端依赖：Vue 3、Vite、Element Plus、Axios
- 数据库：新建 7 张核心表（user、student、teacher、class、course、course_teacher、grade）
- API：对外暴露 RESTful JSON 接口，由前端消费
