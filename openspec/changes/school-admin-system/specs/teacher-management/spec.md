## ADDED Requirements

### Requirement: 创建教师档案
管理员（ADMIN / SUPER_ADMIN）SHALL 能为已有 TEACHER 角色的用户创建教师档案，包含工号、所属院系。
工号 SHALL 全局唯一（含已软删除记录，MVP 阶段不可复用）。
一个用户 SHALL 只能对应一份教师档案。
创建请求体字段：`userId`（必填）、`teacherNo`（必填）、`department`（必填）。

#### Scenario: 创建教师档案成功
- **WHEN** 管理员提交合法的教师信息（工号唯一、user_id 存在且角色为 TEACHER、账号已启用）
- **THEN** 系统创建教师档案，返回档案 ID 及基本信息，HTTP 201

#### Scenario: 工号已存在
- **WHEN** 管理员提交重复工号（含已软删除档案占用的工号）
- **THEN** 系统返回 HTTP 400，错误码 40005，消息 "工号已存在"

#### Scenario: 关联用户不存在
- **WHEN** 管理员提交不存在的 user_id，或 user_id 存在但角色不是 TEACHER，或用户已软删除，或用户已禁用（`status = 0`）
- **THEN** 系统返回 HTTP 400，错误码 40004，消息 "用户不存在"

#### Scenario: 用户已有教师档案
- **WHEN** 管理员为已有教师档案的 user_id 再次创建
- **THEN** 系统返回 HTTP 400，错误码 40005，消息 "该用户已有教师档案"

---

### Requirement: 查询教师列表
管理员（ADMIN / SUPER_ADMIN）SHALL 能分页查询教师列表，支持按院系（`department`，精确匹配）、姓名关键词（`keyword`，匹配 `sys_user.real_name`）过滤。
列表项 SHALL 包含：`id`、`teacherNo`、`realName`、`department`。

#### Scenario: 分页查询教师
- **WHEN** 管理员请求教师列表，传入 `page` 和 `size` 参数
- **THEN** 系统返回当前页教师数据（含 `realName`）及总条数

#### Scenario: 按院系过滤
- **WHEN** 管理员传入 `department` 参数
- **THEN** 系统只返回该院系的教师

#### Scenario: 按姓名关键词过滤
- **WHEN** 管理员传入 `keyword` 参数
- **THEN** 系统只返回 `real_name` 匹配关键词的教师（不匹配 `username`）

#### Scenario: 非管理员查询列表
- **WHEN** 教师或学生角色调用教师列表接口
- **THEN** 系统返回 HTTP 403

---

### Requirement: 查询教师详情
管理员（ADMIN / SUPER_ADMIN）SHALL 能查询单个教师的详细档案。
教师 SHALL 只能查询自己的档案（`teacher.user_id` 等于当前登录用户 ID）。
详情接口权限与存在性判定：**资源不存在** → 40004；**资源存在但非本人**（仅教师角色）→ 403。
详情响应 SHALL 包含：`id`、`userId`、`teacherNo`、`realName`、`department`、`courseNames`（课程名称列表，无关联时返回空数组 `[]`）、`createTime`。

#### Scenario: 管理员查看任意教师详情
- **WHEN** 管理员查询某教师 ID 的详情
- **THEN** 系统返回教师信息及其关联课程名称列表，HTTP 200

#### Scenario: 教师查看自己的档案
- **WHEN** 教师请求本人教师档案的详情
- **THEN** 系统返回完整教师信息及 `courseNames`，HTTP 200

#### Scenario: 教师查看他人档案
- **WHEN** 教师请求非本人的教师详情
- **THEN** 系统返回 HTTP 403

#### Scenario: 教师尚无档案时查询详情
- **WHEN** 教师账号尚未创建教师档案，调用 `GET /api/teachers/{id}`
- **THEN** 若 `id` 对应他人档案，返回 HTTP 403；若 `id` 不存在或已删除，返回 HTTP 400，错误码 40004，消息 "资源不存在"（与管理员查询不存在资源的行为一致，**不**单独返回 403 伪装为「无权访问」）

#### Scenario: 教师档案不存在
- **WHEN** 管理员或教师请求不存在或已删除的教师 ID
- **THEN** 系统返回 HTTP 400，错误码 40004，消息 "资源不存在"

---

### Requirement: 更新教师档案
管理员（ADMIN / SUPER_ADMIN）SHALL 能修改教师的院系信息。
接口为 `PUT /api/teachers/{id}`，采用**部分更新**语义：当前 MVP 仅可更新 `department`；请求体中 `department` 为必填非 null 字段。
教师 SHALL NOT 通过本接口修改自己的档案（非管理员调用返回 HTTP 403）。

#### Scenario: 更新教师院系
- **WHEN** 管理员提交有效的教师 ID 和新院系名称
- **THEN** 系统更新院系，返回 HTTP 200

#### Scenario: 教师尝试更新档案
- **WHEN** 教师角色调用 `PUT /api/teachers/{id}`
- **THEN** 系统返回 HTTP 403

---

### Requirement: 删除教师档案
管理员（ADMIN / SUPER_ADMIN）SHALL 能软删除教师档案。
系统 SHALL 在删除前检查该教师是否仍有关联课程（`course_teacher` 表），若有则拒绝删除。
删除教师档案 **SHALL NOT** 级联软删除关联的 `sys_user` 账号；账号注销由 user-management 模块的 `DELETE /api/users/{id}` 单独处理（与学生档案删除策略一致）。

#### Scenario: 删除无课程关联的教师
- **WHEN** 管理员删除没有关联课程的教师档案
- **THEN** 系统仅逻辑删除 `teacher` 表记录，`sys_user` 保持不变，返回 HTTP 200

#### Scenario: 删除有课程关联的教师
- **WHEN** 管理员删除仍关联课程的教师档案
- **THEN** 系统返回 HTTP 400，错误码 40006，消息 "该教师仍有关联课程，请先移除课程关联"

---

### Requirement: 模块验收与自动化测试
系统 SHALL 满足教师模块在 `tasks.md` §10.3、§10.4 中列出的角色权限与业务校验验收项。
MVP 阶段 **SHALL** 支持通过 Knife4j 或 Postman 完成手工验收；**SHOULD** 补充 `TeacherServiceImpl` 单元测试（`@SpringBootTest` 或纯 Mockito），覆盖以下核心路径：

| 路径 | 验收点 |
|------|--------|
| 创建 | 工号重复 40005、用户无效/禁用 40004、一用户一档案 40005、成功 201 |
| 列表 | 非管理员（教师/学生）403 |
| 详情 | 管理员查任意 200、教师查自己 200、教师查他人 403、不存在 40004 |
| 更新 | 教师调用 PUT 403 |
| 删除 | 有 `course_teacher` 关联 40006、无关联 200；不级联删除 `sys_user` |

#### Scenario: 手工验收通过
- **WHEN** 按 `tasks.md` §10.3、§10.4 逐项执行教师模块用例
- **THEN** 角色边界与错误码均符合本 Spec 约定

#### Scenario: 单元测试覆盖核心校验
- **WHEN** 运行 `TeacherServiceImpl` 相关单元测试
- **THEN** 上表所列核心路径均有对应测试用例且通过
