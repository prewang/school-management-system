package com.school.dto.schoolclass;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SchoolClassCreateRequest {
    @NotBlank(message = "班级名称不能为空")
    private String name;
    @NotBlank(message = "年级不能为空")
    private String grade;
    @NotNull(message = "入学年份不能为空")
    private Integer year;
}
