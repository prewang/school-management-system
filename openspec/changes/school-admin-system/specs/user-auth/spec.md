## ADDED Requirements

### Requirement: 用户登录
系统 SHALL 提供用户名/密码登录接口，验证成功后返回 Access Token 与 Refresh Token。
密码 SHALL 使用 BCrypt 加密存储，登录时比对哈希值。

#### Scenario: 登录成功
- **WHEN** 用户提交正确的用户名和密码
- **THEN** 系统返回 `{ accessToken, refreshToken, role }` 以及 HTTP 200

#### Scenario: 用户名不存在
- **WHEN** 用户提交不存在的用户名
- **THEN** 系统返回 HTTP 401，错误码 40101，消息 "用户名或密码错误"

#### Scenario: 密码错误
- **WHEN** 用户提交正确用户名但错误密码
- **THEN** 系统返回 HTTP 401，错误码 40101，消息 "用户名或密码错误"（不区分原因，防枚举）

#### Scenario: 账号被禁用
- **WHEN** 用户账号 `status` 为禁用状态
- **THEN** 系统返回 HTTP 403，错误码 40003，消息 "账号已被禁用"

---

### Requirement: Token 刷新
系统 SHALL 提供 Refresh Token 刷新接口，有效的 Refresh Token 可换取新的 Access Token。

#### Scenario: Refresh Token 有效
- **WHEN** 客户端携带未过期的 Refresh Token 请求刷新
- **THEN** 系统返回新的 Access Token，HTTP 200

#### Scenario: Refresh Token 已过期
- **WHEN** 客户端携带过期的 Refresh Token 请求刷新
- **THEN** 系统返回 HTTP 401，提示重新登录

---

### Requirement: 用户登出
系统 SHALL 提供登出接口，客户端调用后应清除本地存储的 Token。

#### Scenario: 登出
- **WHEN** 已登录用户调用登出接口
- **THEN** 系统返回 HTTP 200，客户端清除 Token（服务端无状态，不做服务端失效）

---

### Requirement: 接口访问控制
系统 SHALL 对所有非公开接口要求携带有效的 Access Token（Bearer Token）。
系统 SHALL 根据用户角色拒绝无权限的操作。

#### Scenario: 未携带 Token 访问受保护接口
- **WHEN** 请求未携带 Authorization 头
- **THEN** 系统返回 HTTP 401

#### Scenario: Token 已过期
- **WHEN** 请求携带过期的 Access Token
- **THEN** 系统返回 HTTP 401，提示 Token 过期

#### Scenario: 角色无权限
- **WHEN** 学生角色尝试访问仅管理员可见的接口
- **THEN** 系统返回 HTTP 403

---

### Requirement: 公开接口白名单
以下接口 SHALL 无需认证即可访问：`POST /api/auth/login`、`POST /api/auth/refresh`、`GET /doc.html`（Swagger）。

#### Scenario: 访问登录接口无需 Token
- **WHEN** 未登录用户访问 `POST /api/auth/login`
- **THEN** 系统正常处理，不拦截
