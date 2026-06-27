## ADDED Requirements

### Requirement: 创建学生档案
管理员 SHALL 能为已有 STUDENT 角色的用户创建学生档案，包含学号、性别、出生日期、所属班级。
学号 SHALL 全局唯一。
一个用户 SHALL 只能对应一份学生档案。

#### Scenario: 创建学生档案成功
- **WHEN** 管理员提交合法的学生信息（学号唯一、user_id 存在且角色为 STUDENT）
- **THEN** 系统创建学生档案，返回档案 ID，HTTP 201

#### Scenario: 学号已存在
- **WHEN** 管理员提交重复学号
- **THEN** 系统返回 HTTP 400，消息 "学号已存在"

#### Scenario: 关联用户不存在
- **WHEN** 管理员提交不存在的 user_id
- **THEN** 系统返回 HTTP 400，消息 "用户不存在"

---

### Requirement: 查询学生列表
管理员 SHALL 能分页查询学生列表，支持按班级 ID、姓名关键词过滤。

#### Scenario: 分页查询学生
- **WHEN** 管理员请求学生列表
- **THEN** 系统返回当前页学生数据（含所属班级名称）及总条数

#### Scenario: 按班级过滤
- **WHEN** 管理员传入 `classId` 参数
- **THEN** 系统只返回该班级的学生

---

### Requirement: 查询学生详情
管理员和教师 SHALL 能查询单个学生的详细档案。
学生 SHALL 只能查询自己的档案。

#### Scenario: 管理员查看任意学生详情
- **WHEN** 管理员请求某个学生 ID 的详情
- **THEN** 系统返回完整学生信息，HTTP 200

#### Scenario: 学生查看他人档案
- **WHEN** 学生请求非本人的学生详情
- **THEN** 系统返回 HTTP 403

---

### Requirement: 更新学生档案
管理员 SHALL 能修改学生的班级归属、出生日期等信息。

#### Scenario: 更新学生班级
- **WHEN** 管理员提交有效的学生 ID 和新班级 ID
- **THEN** 系统更新学生班级，返回 HTTP 200

---

### Requirement: 删除学生档案
管理员 SHALL 能软删除学生档案。

#### Scenario: 删除学生档案
- **WHEN** 管理员请求删除指定学生档案
- **THEN** 系统逻辑删除，返回 HTTP 200
