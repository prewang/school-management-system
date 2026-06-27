USE school_admin;

-- 超级管理员默认账号: admin / Admin@123456（BCrypt hash，首次登录后请修改）
INSERT IGNORE INTO sys_user (username, password_hash, role, status, create_time, update_time, deleted)
VALUES ('admin',
        '$2a$10$7EqJtq98hPqEX7fNZaFWoOe1YxM4rTCvwB3qNCCTnVVwMfpwpUyGq',
        'SUPER_ADMIN', 1, NOW(), NOW(), 0);
