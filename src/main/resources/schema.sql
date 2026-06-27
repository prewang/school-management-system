CREATE DATABASE IF NOT EXISTS school_admin DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE school_admin;

CREATE TABLE IF NOT EXISTS sys_user (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    username      VARCHAR(50)  NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(20)  NOT NULL COMMENT 'SUPER_ADMIN/ADMIN/TEACHER/STUDENT',
    status        TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '0=禁用,1=启用',
    create_time   DATETIME     NOT NULL,
    update_time   DATETIME     NOT NULL,
    deleted       TINYINT(1)   NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `class` (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(100) NOT NULL,
    grade       VARCHAR(50)  NOT NULL,
    year        INT          NOT NULL,
    create_time DATETIME     NOT NULL,
    update_time DATETIME     NOT NULL,
    deleted     TINYINT(1)   NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS student (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    user_id     BIGINT       NOT NULL,
    class_id    BIGINT       NOT NULL,
    student_no  VARCHAR(50)  NOT NULL,
    name        VARCHAR(100) NOT NULL,
    gender      TINYINT(1)   NOT NULL DEFAULT 2 COMMENT '0=女,1=男,2=未知',
    create_time DATETIME     NOT NULL,
    update_time DATETIME     NOT NULL,
    deleted     TINYINT(1)   NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_id (user_id),
    UNIQUE KEY uk_student_no (student_no),
    INDEX idx_student_class_id (class_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS teacher (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    user_id     BIGINT       NOT NULL,
    teacher_no  VARCHAR(50)  NOT NULL,
    name        VARCHAR(100) NOT NULL,
    gender      TINYINT(1)   NOT NULL DEFAULT 2 COMMENT '0=女,1=男,2=未知',
    create_time DATETIME     NOT NULL,
    update_time DATETIME     NOT NULL,
    deleted     TINYINT(1)   NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_id (user_id),
    UNIQUE KEY uk_teacher_no (teacher_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS course (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(200) NOT NULL,
    code        VARCHAR(50)  NOT NULL,
    credit      INT          NOT NULL,
    create_time DATETIME     NOT NULL,
    update_time DATETIME     NOT NULL,
    deleted     TINYINT(1)   NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS course_teacher (
    id         BIGINT NOT NULL AUTO_INCREMENT,
    course_id  BIGINT NOT NULL,
    teacher_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_course_teacher (course_id, teacher_id),
    INDEX idx_ct_teacher_id (teacher_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS grade (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    student_id  BIGINT       NOT NULL,
    course_id   BIGINT       NOT NULL,
    score       DECIMAL(5,2) NOT NULL COMMENT '0.00~100.00',
    semester    VARCHAR(10)  NOT NULL COMMENT 'YYYY-1 或 YYYY-2',
    create_time DATETIME     NOT NULL,
    update_time DATETIME     NOT NULL,
    deleted     TINYINT(1)   NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_student_course_semester (student_id, course_id, semester),
    INDEX idx_grade_student_id (student_id),
    INDEX idx_grade_course_id  (course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
