## ADDED Requirements

### Requirement: 创建学生档案
管理员 SHALL 能为已有 STUDENT 角色的用户创建学生档案，包含学号、性别、出生日期、所属班级。
学号 SHALL 全局唯一。
一个用户 SHALL 只能对应一份学生档案。
创建请求体字段：`userId`（必填）、`classId`（必填）、`studentNo`（必填）、`gender`（可选，默认 2=未知）、`birthDate`（可选）。

#### Scenario: 创建学生档案成功
- **WHEN** 管理员提交合法的学生信息（学号唯一、user_id 存在且角色为 STUDENT、class_id 对应班级存在）
- **THEN** 系统创建学生档案，返回档案 ID 及基本信息，HTTP 201

#### Scenario: 学号已存在
- **WHEN** 管理员提交重复学号
- **THEN** 系统返回 HTTP 400，错误码 40005，消息 "学号已存在"

#### Scenario: 关联用户不存在
- **WHEN** 管理员提交不存在的 user_id，或 user_id 存在但角色不是 STUDENT，或用户已软删除
- **THEN** 系统返回 HTTP 400，错误码 40004，消息 "用户不存在"

#### Scenario: 用户已有学生档案
- **WHEN** 管理员为已有学生档案的 user_id 再次创建
- **THEN** 系统返回 HTTP 400，错误码 40005，消息 "该用户已有学生档案"

#### Scenario: 班级不存在
- **WHEN** 管理员提交不存在的 class_id
- **THEN** 系统返回 HTTP 400，错误码 40004，消息 "班级不存在"

---

### Requirement: 查询学生列表
管理员 SHALL 能分页查询学生列表，支持按班级 ID（`classId`）、姓名关键词（`keyword`，匹配 `sys_user.real_name`）过滤。
列表项 SHALL 包含：`id`、`studentNo`、`realName`、`gender`、`classId`、`className`。

#### Scenario: 分页查询学生
- **WHEN** 管理员请求学生列表，传入 `page` 和 `size` 参数
- **THEN** 系统返回当前页学生数据（含 `realName`、所属班级名称 `className`）及总条数

#### Scenario: 按班级过滤
- **WHEN** 管理员传入 `classId` 参数
- **THEN** 系统只返回该班级的学生

#### Scenario: 按姓名关键词过滤
- **WHEN** 管理员传入 `keyword` 参数
- **THEN** 系统只返回 `real_name` 匹配关键词的学生

#### Scenario: 非管理员查询列表
- **WHEN** 教师或学生角色调用学生列表接口
- **THEN** 系统返回 HTTP 403

---

### Requirement: 查询学生详情
管理员和教师 SHALL 能查询单个学生的详细档案。
学生 SHALL 只能查询自己的档案（`student.user_id` 等于当前登录用户 ID）。
详情响应 SHALL 包含：`id`、`userId`、`studentNo`、`realName`、`gender`、`birthDate`、`classId`、`className`、`createTime`。

#### Scenario: 管理员查看任意学生详情
- **WHEN** 管理员请求某个学生 ID 的详情
- **THEN** 系统返回完整学生信息，HTTP 200

#### Scenario: 教师查看学生详情
- **WHEN** 教师请求某个学生 ID 的详情
- **THEN** 系统返回完整学生信息，HTTP 200

#### Scenario: 学生查看自己的档案
- **WHEN** 学生请求本人学生档案的详情
- **THEN** 系统返回完整学生信息，HTTP 200

#### Scenario: 学生查看他人档案
- **WHEN** 学生请求非本人的学生详情
- **THEN** 系统返回 HTTP 403

#### Scenario: 学生档案不存在
- **WHEN** 请求不存在或已删除的学生 ID
- **THEN** 系统返回 HTTP 400，错误码 40004，消息 "资源不存在"

---

### Requirement: 更新学生档案
管理员 SHALL 能修改学生的班级归属、性别、出生日期等信息。
接口为 `PUT /api/students/{id}`，采用**部分更新**语义：仅更新请求体中传入的非 null 字段；至少须传入一个可更新字段（`classId`、`gender`、`birthDate`）。
若传入 `classId`，系统 SHALL 校验班级存在。

#### Scenario: 更新学生班级
- **WHEN** 管理员提交有效的学生 ID 和新班级 ID
- **THEN** 系统更新学生班级，返回 HTTP 200

#### Scenario: 更新班级不存在
- **WHEN** 管理员提交不存在的 class_id
- **THEN** 系统返回 HTTP 400，错误码 40004，消息 "班级不存在"

#### Scenario: 未传入任何可更新字段
- **WHEN** 管理员提交空的更新请求体（无可更新字段）
- **THEN** 系统返回 HTTP 400，错误码 40000，消息 "参数校验失败"

---

### Requirement: 删除学生档案
管理员 SHALL 能软删除学生档案。
系统 SHALL 在删除前检查该学生是否仍有成绩记录，若有则拒绝删除。

#### Scenario: 删除学生档案
- **WHEN** 管理员请求删除没有成绩记录的学生档案
- **THEN** 系统逻辑删除，返回 HTTP 200

#### Scenario: 删除有成绩记录的学生
- **WHEN** 管理员尝试删除仍有成绩记录的学生档案
- **THEN** 系统返回 HTTP 400，错误码 40006，消息 "该学生存在成绩记录，无法删除"
