## ADDED Requirements

### Requirement: 创建课程
管理员 SHALL 能创建课程，包含课程名称、课程代码、学分。
课程代码 SHALL 全局唯一（含已软删除记录占用的代码，MVP 阶段不可复用）。

#### Scenario: 创建课程成功
- **WHEN** 管理员提交合法的课程信息（课程代码唯一）
- **THEN** 系统创建课程，返回课程 ID，HTTP 201

#### Scenario: 课程代码重复
- **WHEN** 管理员提交已存在的课程代码
- **THEN** 系统返回 HTTP 400，错误码 40005，消息 "课程代码已存在"

---

### Requirement: 查询课程列表
管理员 SHALL 能分页查询全部课程列表，支持按课程名称关键词（keyword）过滤。
列表项 SHALL 包含关联教师姓名（teacherNames，无关联时返回空数组）。

> 对外错误响应：`HTTP 400` 等表述指响应体 `code` 字段（如 40005、40006），由 GlobalExceptionHandler 统一封装，见 design 决策 4。

#### Scenario: 管理员查询全部课程
- **WHEN** 管理员请求 `GET /api/courses`
- **THEN** 系统返回所有未删除课程及关联教师姓名

#### Scenario: 按名称关键词过滤
- **WHEN** 管理员传入 `keyword=数学`
- **THEN** 系统只返回课程名称匹配该关键词的记录

#### Scenario: 非管理员查询全部课程
- **WHEN** 教师或学生请求 `GET /api/courses`
- **THEN** 系统返回 HTTP 403

---

### Requirement: 教师查询自己的课程
教师 SHALL 能通过 `GET /api/courses/my` 分页查询本人负责的课程列表，支持 keyword 过滤。
响应 SHALL 复用 `CoursePageResponse`（与 `GET /api/courses` 列表项字段一致，含 `teacherNames` 全量关联教师姓名，无关联时 `[]`）。

#### Scenario: 教师查询自己的课程
- **WHEN** 教师请求 `GET /api/courses/my`
- **THEN** 系统只返回该教师负责的课程，每项为 `CoursePageResponse`（含 `teacherNames`）

#### Scenario: 非教师查询 my 接口
- **WHEN** 管理员或学生请求 `GET /api/courses/my`
- **THEN** 系统返回 HTTP 403

---

### Requirement: 分配课程教师
管理员 SHALL 能将一门课程分配给一个或多个教师（通过 course_teacher 中间表）。
同一教师不能重复分配到同一课程。

#### Scenario: 分配课程给教师
- **WHEN** 管理员提交课程 ID 和教师 ID 列表
- **THEN** 系统创建关联记录，返回 HTTP 200

#### Scenario: 重复分配
- **WHEN** 管理员将同一教师分配到已关联的课程
- **THEN** 系统忽略重复项，返回 HTTP 200（幂等）

#### Scenario: 教师不存在
- **WHEN** 管理员提交的教师 ID 不存在或已软删除
- **THEN** 系统返回 HTTP 400，错误码 40004，消息 "资源不存在"

#### Scenario: 课程不存在
- **WHEN** 管理员向不存在或已软删除的课程 ID 提交教师 ID 列表
- **THEN** 系统返回 HTTP 400，错误码 40004，消息 "资源不存在"

---

### Requirement: 移除课程教师关联
管理员 SHALL 能移除某课程与某教师的关联。
系统 SHALL 检查该课程下是否存在成绩记录（grade 表按 course_id），若有则拒绝移除。

#### Scenario: 移除无成绩关联
- **WHEN** 管理员移除教师课程关联，且该课程下无成绩记录
- **THEN** 系统删除关联记录，返回 HTTP 200

#### Scenario: 课程或关联不存在
- **WHEN** 管理员移除关联，但课程不存在、已软删除，或该课程与教师无关联记录
- **THEN** 系统返回 HTTP 400，错误码 40004，消息 "资源不存在"

#### Scenario: 移除有成绩关联
- **WHEN** 管理员移除教师课程关联，但该课程下已有成绩记录
- **THEN** 系统返回 HTTP 400，错误码 40006，消息 "该课程已有成绩数据，无法移除教师关联"

---

### Requirement: 更新课程信息
管理员 SHALL 能修改课程名称、学分（部分更新，至少传入一个非 null 字段；课程代码不可修改）。

#### Scenario: 更新课程学分
- **WHEN** 管理员提交有效课程 ID 和新学分值
- **THEN** 系统更新学分，返回 HTTP 200

#### Scenario: 部分更新课程
- **WHEN** 管理员提交有效课程 ID 及 name 或 credit（至少一个非 null）
- **THEN** 系统仅更新传入字段，返回 HTTP 200

#### Scenario: 更新请求无有效字段
- **WHEN** 管理员提交的课程更新请求中 name 与 credit 均为 null
- **THEN** 系统返回 HTTP 400，错误码 40000

#### Scenario: 课程不存在或已删除
- **WHEN** 管理员更新不存在或已软删除的课程 ID
- **THEN** 系统返回 HTTP 400，错误码 40004，消息 "资源不存在"

---

### Requirement: 删除课程
管理员 SHALL 能软删除课程。
系统 SHALL 在删除前检查是否有成绩记录，若有则拒绝删除。

#### Scenario: 删除无成绩的课程
- **WHEN** 管理员删除没有成绩记录的课程
- **THEN** 系统逻辑删除，返回 HTTP 200

#### Scenario: 课程不存在或已删除
- **WHEN** 管理员删除不存在或已软删除的课程 ID
- **THEN** 系统返回 HTTP 400，错误码 40004，消息 "资源不存在"

#### Scenario: 删除有成绩的课程
- **WHEN** 管理员尝试删除有成绩记录的课程
- **THEN** 系统返回 HTTP 400，错误码 40006，消息 "该课程已有成绩数据，无法删除"

---

### Requirement: 模块验收与自动化测试
MVP 阶段须满足 tasks.md §8 手工验收项。**不阻塞** MVP 合并：建议（optional）补充 `CourseServiceImpl` 单元测试（tasks 8.9），覆盖创建重复代码（40005）、分配幂等、移除/删除关联校验（40006）、列表与 my 接口权限（403）、资源不存在（40004）。
