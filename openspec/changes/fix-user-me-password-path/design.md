## Context

用户管理模块的修改密码接口当前规划路径为 `PUT /api/users/password`。该路径存在语义不清晰问题：`/api/users` 前缀通常表示"管理员对用户资源的操作"，而修改自己密码属于"当前登录用户的自身操作"，两者混用同一前缀会导致路径语义模糊。

## Goals / Non-Goals

**Goals:**
- 将修改密码接口路径变更为 `PUT /api/users/me/password`，语义明确表达"当前用户操作自身"
- 同步更新 school-admin-system 变更中的 design.md 路径规范表和 tasks.md 任务描述
- 为后续可能新增的 `GET /api/users/me`（当前用户信息）等接口预留 `/me` 前缀约定

**Non-Goals:**
- 不修改接口的请求参数、响应格式、权限要求
- 不新增 `/me` 系列的其他接口（仅预留前缀，不在本次实现）

## Decisions

**路径选择 `PUT /api/users/me/password` 而非其他方案**

- 备选 A `PUT /api/users/password`（原路径）：`password` 会被 Spring MVC 解析时与 `{id}` 产生视觉混淆，实现者需要了解精确段优先的原则，增加认知负担
- 备选 B `POST /api/users/password/change`：改变 HTTP 方法语义，且路径层级冗余
- 选择 `PUT /api/users/me/password`：`me` 明确区分"当前登录用户"与"管理员管理任意用户"，与业界 REST API 惯例一致（如 GitHub `/user`、Slack `/users.profile.set`）

## Risks / Trade-offs

- **BREAKING 变更** → 前后端联调阶段须同步修改前端调用地址；当前处于规划阶段，无已上线代码，变更成本为零
- Spring Security 白名单无需调整（`/users/**` 已覆盖 `/users/me/password`）
