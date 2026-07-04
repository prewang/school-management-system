package com.school.dto.course;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CourseUpdateRequest {
    @Size(min = 1, max = 200, message = "课程名称不能为空")
    private String name;
    @Min(value = 1, message = "学分最小为 1")
    private Integer credit;
}
