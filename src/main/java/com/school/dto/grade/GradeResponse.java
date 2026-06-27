package com.school.dto.grade;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class GradeResponse {
    private Long id;
    private Long studentId;
    private Long courseId;
    private BigDecimal score;
    private String semester;
    private LocalDateTime createTime;
}
