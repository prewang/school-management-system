## ADDED Requirements

### Requirement: 创建教师档案
管理员 SHALL 能为已有 TEACHER 角色的用户创建教师档案，包含工号、所属院系。
工号 SHALL 全局唯一。

#### Scenario: 创建教师档案成功
- **WHEN** 管理员提交合法的教师信息（工号唯一、user_id 存在且角色为 TEACHER）
- **THEN** 系统创建教师档案，返回档案 ID，HTTP 201

#### Scenario: 工号已存在
- **WHEN** 管理员提交重复工号
- **THEN** 系统返回 HTTP 400，消息 "工号已存在"

---

### Requirement: 查询教师列表
管理员 SHALL 能分页查询教师列表，支持按院系、姓名关键词过滤。

#### Scenario: 分页查询教师
- **WHEN** 管理员请求教师列表
- **THEN** 系统返回当前页教师数据及总条数

---

### Requirement: 查询教师详情
管理员 SHALL 能查询单个教师的详细档案，含其负责的课程列表。
教师 SHALL 能查询自己的档案。

#### Scenario: 查询教师详情含课程
- **WHEN** 管理员查询某教师详情
- **THEN** 系统返回教师信息及其关联课程名称列表，HTTP 200

---

### Requirement: 更新教师档案
管理员 SHALL 能修改教师的院系等信息。

#### Scenario: 更新教师院系
- **WHEN** 管理员提交有效的教师 ID 和新院系名称
- **THEN** 系统更新院系，返回 HTTP 200

---

### Requirement: 删除教师档案
管理员 SHALL 能软删除教师档案。
系统 SHALL 在删除前检查该教师是否仍有关联课程，若有则拒绝删除。

#### Scenario: 删除无课程关联的教师
- **WHEN** 管理员删除没有关联课程的教师档案
- **THEN** 系统逻辑删除，返回 HTTP 200

#### Scenario: 删除有课程关联的教师
- **WHEN** 管理员删除仍关联课程的教师档案
- **THEN** 系统返回 HTTP 400，消息 "该教师仍有关联课程，请先移除课程关联"
