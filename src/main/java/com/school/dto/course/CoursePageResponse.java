package com.school.dto.course;

import lombok.Data;

@Data
public class CoursePageResponse {
    private Long id;
    private String name;
    private String code;
    private Integer credit;
}
