## Why

`PUT /api/users/password` 路径语义不明确，且无法区分"管理员操作用户资源"与"当前用户修改自己的密码"。将路径改为 `PUT /api/users/me/password`，同时为后续"当前用户自身操作"系列接口（如 `GET /api/users/me`）预留统一的 `/me` 前缀。

## What Changes

- 将修改密码接口路径从 `PUT /api/users/password` 变更为 `PUT /api/users/me/password`
- `design.md` 路径规范表中用户管理模块路径同步更新
- `tasks.md` 中 4.6 条目路径同步更新

## Capabilities

### New Capabilities

无

### Modified Capabilities

- `user-management`：修改密码接口 URL 由 `/api/users/password` 变更为 `/api/users/me/password`，接口行为和权限不变

## Impact

- 影响文件：`openspec/changes/school-admin-system/specs/user-management/spec.md`、`openspec/changes/school-admin-system/design.md`、`openspec/changes/school-admin-system/tasks.md`
- **BREAKING**：接口路径变更，前端联调阶段须同步修改调用地址
- 无数据库变更，无认证逻辑变更
