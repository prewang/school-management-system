package com.school.dto.teacher;

import lombok.Data;

@Data
public class TeacherPageResponse {
    private Long id;
    private String teacherNo;
    private String name;
    private Integer gender;
}
