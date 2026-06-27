## ADDED Requirements

### Requirement: 创建课程
管理员 SHALL 能创建课程，包含课程名称、课程代码、学分。
课程代码 SHALL 全局唯一。

#### Scenario: 创建课程成功
- **WHEN** 管理员提交合法的课程信息（课程代码唯一）
- **THEN** 系统创建课程，返回课程 ID，HTTP 201

#### Scenario: 课程代码重复
- **WHEN** 管理员提交已存在的课程代码
- **THEN** 系统返回 HTTP 400，消息 "课程代码已存在"

---

### Requirement: 查询课程列表
管理员和教师 SHALL 能查询课程列表，支持按课程名称关键词过滤。
教师 SHALL 能查询自己负责的课程列表（`/api/courses/my`）。

#### Scenario: 管理员查询全部课程
- **WHEN** 管理员请求课程列表
- **THEN** 系统返回所有未删除课程及关联教师姓名

#### Scenario: 教师查询自己的课程
- **WHEN** 教师请求 `GET /api/courses/my`
- **THEN** 系统只返回该教师负责的课程

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

---

### Requirement: 移除课程教师关联
管理员 SHALL 能移除某课程与某教师的关联。
系统 SHALL 检查该教师在该课程下是否有成绩记录，若有则拒绝移除。

#### Scenario: 移除无成绩关联
- **WHEN** 管理员移除教师课程关联，且该关联无成绩记录
- **THEN** 系统删除关联记录，返回 HTTP 200

#### Scenario: 移除有成绩关联
- **WHEN** 管理员移除教师课程关联，但该课程已有成绩记录
- **THEN** 系统返回 HTTP 400，消息 "该教师在此课程下已有成绩记录，无法移除"

---

### Requirement: 更新课程信息
管理员 SHALL 能修改课程名称、学分。

#### Scenario: 更新课程学分
- **WHEN** 管理员提交有效课程 ID 和新学分值
- **THEN** 系统更新学分，返回 HTTP 200

---

### Requirement: 删除课程
管理员 SHALL 能软删除课程。
系统 SHALL 在删除前检查是否有成绩记录，若有则拒绝删除。

#### Scenario: 删除无成绩的课程
- **WHEN** 管理员删除没有成绩记录的课程
- **THEN** 系统逻辑删除，返回 HTTP 200

#### Scenario: 删除有成绩的课程
- **WHEN** 管理员尝试删除有成绩记录的课程
- **THEN** 系统返回 HTTP 400，消息 "该课程已有成绩数据，无法删除"
