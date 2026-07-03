package com.school.mapper.row;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class StudentDetailRow {
    private Long id;
    private Long userId;
    private String studentNo;
    private String realName;
    private Integer gender;
    private LocalDate birthDate;
    private Long classId;
    private String className;
    private LocalDateTime createTime;
}
