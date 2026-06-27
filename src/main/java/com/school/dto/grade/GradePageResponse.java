package com.school.dto.grade;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class GradePageResponse {
    private Long id;
    private Long studentId;
    private Long courseId;
    private BigDecimal score;
    private String semester;
}
