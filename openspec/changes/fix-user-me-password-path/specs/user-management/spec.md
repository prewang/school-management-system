## MODIFIED Requirements

### Requirement: 更新用户信息
管理员 SHALL 能修改用户的真实姓名、角色和账号状态（启用/禁用）。
用户 SHALL 能修改自己的密码（旧密码验证后才能修改），接口路径为 `PUT /api/users/me/password`。

#### Scenario: 管理员修改用户角色
- **WHEN** 管理员提交有效的用户 ID 和新角色
- **THEN** 系统更新角色，返回 HTTP 200

#### Scenario: 用户修改自己密码
- **WHEN** 用户提交正确的旧密码和新密码至 `PUT /api/users/me/password`
- **THEN** 系统更新密码哈希，返回 HTTP 200

#### Scenario: 旧密码验证失败
- **WHEN** 用户提交的旧密码与数据库不匹配
- **THEN** 系统返回 HTTP 400，消息 "原密码错误"
