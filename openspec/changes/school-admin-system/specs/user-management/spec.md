## ADDED Requirements

### Requirement: 创建用户
管理员 SHALL 能创建新用户账号，指定用户名、密码、真实姓名和角色（ADMIN / TEACHER / STUDENT）。
用户名 SHALL 全局唯一。

#### Scenario: 创建用户成功
- **WHEN** 管理员提交合法的用户信息（用户名唯一、角色合法）
- **THEN** 系统创建用户，返回新用户 ID 和基本信息，HTTP 201

#### Scenario: 用户名重复
- **WHEN** 管理员提交已存在的用户名
- **THEN** 系统返回 HTTP 400，错误码 40005，消息 "用户名已存在"

#### Scenario: 非管理员尝试创建用户
- **WHEN** 教师或学生角色调用创建用户接口
- **THEN** 系统返回 HTTP 403

---

### Requirement: 查询用户列表
管理员 SHALL 能分页查询用户列表，支持按角色、用户名关键词过滤。

#### Scenario: 分页查询用户
- **WHEN** 管理员请求用户列表，传入 page 和 size 参数
- **THEN** 系统返回当前页用户数据及总条数

#### Scenario: 按角色过滤
- **WHEN** 管理员传入 `role=TEACHER` 过滤参数
- **THEN** 系统只返回教师角色的用户

---

### Requirement: 更新用户信息
管理员 SHALL 能修改用户的真实姓名、角色和账号状态（启用/禁用），接口为 `PUT /api/users/{id}`，`realName`、`role`、`status` 三个字段均为必填（全量 PUT 语义，前端须传入当前值）。
用户 SHALL 能修改自己的密码（旧密码验证后才能修改），接口为 `PUT /api/users/me/password`。

#### Scenario: 管理员修改用户角色
- **WHEN** 管理员提交有效的用户 ID 和新角色
- **THEN** 系统更新角色，返回 HTTP 200

#### Scenario: 用户修改自己密码
- **WHEN** 用户提交正确的旧密码和新密码至 `PUT /api/users/me/password`
- **THEN** 系统更新密码哈希，返回 HTTP 200

#### Scenario: 旧密码验证失败
- **WHEN** 用户提交的旧密码与数据库不匹配
- **THEN** 系统返回 HTTP 400，消息 "原密码错误"

---

### Requirement: 删除用户
管理员 SHALL 能软删除用户账号（逻辑删除，`is_deleted=1`）。
系统 SHALL 禁止删除自身账号。

#### Scenario: 管理员删除用户
- **WHEN** 管理员请求删除指定用户
- **THEN** 系统逻辑删除该用户，返回 HTTP 200

#### Scenario: 尝试删除自身
- **WHEN** 管理员尝试删除自己的账号
- **THEN** 系统返回 HTTP 400，消息 "不能删除当前登录账号"
