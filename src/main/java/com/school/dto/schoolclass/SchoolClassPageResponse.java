package com.school.dto.schoolclass;

import lombok.Data;

@Data
public class SchoolClassPageResponse {
    private Long id;
    private String name;
    private String grade;
    private Integer year;
}
