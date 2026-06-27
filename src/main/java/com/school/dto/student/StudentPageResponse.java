package com.school.dto.student;

import lombok.Data;

@Data
public class StudentPageResponse {
    private Long id;
    private String studentNo;
    private String name;
    private Integer gender;
    private Long classId;
}
