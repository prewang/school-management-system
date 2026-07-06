package com.school.dto.schoolclass;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SchoolClassUpdateRequest {
    @Size(min = 1, max = 100, message = "班级名称不能为空")
    private String name;
    @Size(min = 1, max = 50, message = "年级不能为空")
    private String grade;
    private Integer year;
}
