package com.school.dto.grade;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class GradeCreateRequest {
    @NotNull(message = "学生 ID 不能为空")
    private Long studentId;
    @NotNull(message = "课程 ID 不能为空")
    private Long courseId;
    @NotNull(message = "成绩不能为空")
    @DecimalMin(value = "0.00", message = "分数须在 0~100 之间")
    @DecimalMax(value = "100.00", message = "分数须在 0~100 之间")
    private BigDecimal score;
    @NotBlank(message = "学期不能为空")
    @Pattern(regexp = "\\d{4}-[12]", message = "学期格式为 YYYY-1 或 YYYY-2")
    private String semester;
}
