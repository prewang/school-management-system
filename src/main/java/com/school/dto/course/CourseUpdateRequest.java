package com.school.dto.course;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class CourseUpdateRequest {
    private String name;
    @Min(value = 1, message = "学分最小为 1")
    private Integer credit;
}
