package com.school.dto.grade;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class GradeUpdateRequest {

    @NotNull(message = "成绩不能为空")
    @DecimalMin(value = "0.00", message = "分数须在 0~100 之间")
    @DecimalMax(value = "100.00", message = "分数须在 0~100 之间")
    private BigDecimal score;
}
