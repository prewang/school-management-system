package com.school.mapper.row;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TeacherDetailRow {
    private Long id;
    private Long userId;
    private String teacherNo;
    private String realName;
    private String department;
    private LocalDateTime createTime;
}
