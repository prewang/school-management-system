package com.school.mapper.row;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class GradeListRow {

    private Long id;
    private Long studentId;
    private String studentRealName;
    private Long courseId;
    private String courseName;
    private BigDecimal score;
    private String semester;
}
