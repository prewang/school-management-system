package com.school.dto.student;

import lombok.Data;

@Data
public class StudentUpdateRequest {
    private Long classId;
    private String name;
    private Integer gender;
}
