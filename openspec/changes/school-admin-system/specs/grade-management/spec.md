## ADDED Requirements

### Requirement: 教师录入成绩
教师（`TEACHER` 角色）SHALL 能为自己负责的课程录入学生成绩，包含学生 ID、课程 ID、分数、学期。
管理员（`ADMIN` / `SUPER_ADMIN`）SHALL NOT 代为录入成绩（MVP 阶段）。
分数 SHALL 为 0~100 之间的数值（可含最多两位小数，与 `DECIMAL(5,2)` 一致）。
系统 SHALL 拒绝教师操作不属于自己的课程。
MVP 阶段同一学生同一课程同一学期 SHALL 只允许一条成绩记录（唯一约束）。
MVP 阶段 SHALL NOT 提供 `GET /api/grades/{id}` 详情接口与 `DELETE /api/grades/{id}` 删除接口。

#### Scenario: 教师录入成绩成功
- **WHEN** 教师提交自己课程下学生的成绩，学期格式合法（如 "2024-1"）
- **THEN** 系统创建成绩记录，返回成绩 ID，HTTP 201

#### Scenario: 教师操作他人课程
- **WHEN** 教师提交不属于自己的课程 ID
- **THEN** 系统返回 HTTP 403，业务码 40301，消息 "无权操作该课程成绩"

#### Scenario: 成绩超出范围
- **WHEN** 教师提交分数 < 0 或 > 100
- **THEN** 系统返回 HTTP 400，消息 "分数须在 0~100 之间"

#### Scenario: 重复录入同一成绩
- **WHEN** 同一学生同一课程同一学期已有成绩，教师再次录入
- **THEN** 系统返回 HTTP 400，消息 "该学生本学期此课程成绩已存在，请使用修改接口"

#### Scenario: 学生或课程不存在
- **WHEN** 教师提交的 `studentId` 或 `courseId` 对应资源不存在或已软删除
- **THEN** 系统返回 HTTP 400，业务码 40004，消息 "资源不存在"

#### Scenario: 管理员尝试录入成绩
- **WHEN** `ADMIN` 或 `SUPER_ADMIN` 调用 `POST /api/grades`
- **THEN** 系统返回 HTTP 403

---

### Requirement: 教师修改成绩
教师（`TEACHER` 角色）SHALL 能修改自己课程下已录入的学生成绩（仅更新 `score` 字段）。
管理员 SHALL NOT 代为修改成绩（MVP 阶段）。

#### Scenario: 修改成绩成功
- **WHEN** 教师提交合法的成绩 ID 和新分数，且该成绩属于自己的课程
- **THEN** 系统更新分数，返回 HTTP 200

#### Scenario: 修改他人课程的成绩
- **WHEN** 教师提交不属于自己课程的成绩 ID
- **THEN** 系统返回 HTTP 403，业务码 40301，消息 "无权操作该课程成绩"

#### Scenario: 成绩不存在
- **WHEN** 教师提交的成绩 ID 不存在或已软删除
- **THEN** 系统返回 HTTP 400，业务码 40004，消息 "资源不存在"

#### Scenario: 管理员尝试修改成绩
- **WHEN** `ADMIN` 或 `SUPER_ADMIN` 调用 `PUT /api/grades/{id}`
- **THEN** 系统返回 HTTP 403

---

### Requirement: 学生查询自己的成绩
学生 SHALL 能分页查询自己所有课程的成绩列表，支持按学期过滤。
接口为 `GET /api/grades/my`，复用全局 `PageRequest` / `PageResult` 约定（默认 10 条/页，最大 100 条/页）。

#### Scenario: 学生分页查询成绩列表
- **WHEN** 已登录学生请求 `GET /api/grades/my`，传入 `page` 和 `size` 参数
- **THEN** 系统返回该学生当前页成绩（含课程名称、学期、分数）及分页元数据

#### Scenario: 按学期过滤
- **WHEN** 学生传入 `semester=2024-1` 参数
- **THEN** 系统只返回该学期的成绩

#### Scenario: 学生访问管理列表接口
- **WHEN** 学生角色调用 `GET /api/grades`（非 `/my`）
- **THEN** 系统返回 HTTP 403

---

### Requirement: 管理员查询成绩
管理员 SHALL 能分页查询任意学生的成绩，支持按学生 ID、课程 ID、班级 ID、学期过滤。
接口为 `GET /api/grades`，复用全局 `PageRequest` / `PageResult` 约定。

#### Scenario: 管理员查询指定学生全部成绩
- **WHEN** 管理员请求 `GET /api/grades?studentId=<id>`
- **THEN** 系统返回该学生成绩分页列表，含课程名称、学期、分数

#### Scenario: 管理员按课程查询全班成绩
- **WHEN** 管理员传入 `courseId` 和可选 `classId`
- **THEN** 系统返回该课程下（指定班级的）学生成绩分页列表，含学生姓名

---

### Requirement: 教师查询本课程成绩
教师 SHALL 能分页查询自己课程下所有学生的成绩列表，支持按学期过滤。
调用 `GET /api/grades` 时 `courseId` 为**必填**参数；教师 SHALL NOT 使用 `classId` 或 `studentId` 过滤参数。

#### Scenario: 教师查询课程成绩
- **WHEN** 教师请求 `GET /api/grades?courseId=<id>`，且该课程属于本人
- **THEN** 系统返回该课程所有学生的成绩分页列表（含学生姓名）

#### Scenario: 教师未传 courseId
- **WHEN** 教师请求 `GET /api/grades` 且未传入 `courseId`
- **THEN** 系统返回 HTTP 400，业务码 40000，消息 "courseId 不能为空"

#### Scenario: 教师查询他人课程成绩
- **WHEN** 教师传入不属于本人的 `courseId`
- **THEN** 系统返回 HTTP 403，业务码 40301，消息 "无权操作该课程成绩"
