## ADDED Requirements

### Requirement: 创建班级
管理员 SHALL 能创建班级，包含班级名称、年级（如"2024级"）、入学年份。
同一年份内班级名称 SHALL 唯一。

#### Scenario: 创建班级成功
- **WHEN** 管理员提交合法的班级信息
- **THEN** 系统创建班级，返回班级 ID，HTTP 201

#### Scenario: 班级名称重复（同年份）
- **WHEN** 管理员在同一年份内提交重复的班级名称
- **THEN** 系统返回 HTTP 400，消息 "该年份下班级名称已存在"

---

### Requirement: 查询班级列表
管理员和教师 SHALL 能查询班级列表，支持按年份过滤。

#### Scenario: 查询全部班级
- **WHEN** 管理员请求班级列表
- **THEN** 系统返回所有未删除的班级，含每班学生数量

---

### Requirement: 查询班级详情
管理员 SHALL 能查询指定班级的详情，含该班所有学生列表。

#### Scenario: 查询班级详情
- **WHEN** 管理员请求某班级 ID 的详情
- **THEN** 系统返回班级信息及该班学生列表，HTTP 200

---

### Requirement: 更新班级信息
管理员 SHALL 能修改班级名称、年级等字段。

#### Scenario: 更新班级名称
- **WHEN** 管理员提交有效的班级 ID 和新名称
- **THEN** 系统更新班级名称，返回 HTTP 200

---

### Requirement: 删除班级
管理员 SHALL 能软删除班级。
系统 SHALL 在删除前检查班级是否仍有学生归属，若有则拒绝删除。

#### Scenario: 删除空班级
- **WHEN** 管理员删除没有学生的班级
- **THEN** 系统逻辑删除，返回 HTTP 200

#### Scenario: 删除有学生的班级
- **WHEN** 管理员尝试删除仍有学生的班级
- **THEN** 系统返回 HTTP 400，消息 "该班级下仍有学生，请先转移学生"
