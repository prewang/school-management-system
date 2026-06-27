package com.school.dto.grade;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class GradeUpdateRequest {
    @DecimalMin(value = "0.00", message = "成绩最小 0")
    @DecimalMax(value = "100.00", message = "成绩最大 100")
    private BigDecimal score;
}
