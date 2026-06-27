package com.school.dto.course;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CourseCreateRequest {
    @NotBlank(message = "课程名称不能为空")
    private String name;
    @NotBlank(message = "课程代码不能为空")
    private String code;
    @NotNull(message = "学分不能为空")
    @Min(value = 1, message = "学分最小为 1")
    private Integer credit;
}
