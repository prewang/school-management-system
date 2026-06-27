package com.school.dto.schoolclass;

import lombok.Data;

@Data
public class SchoolClassUpdateRequest {
    private String name;
    private String grade;
    private Integer year;
}
