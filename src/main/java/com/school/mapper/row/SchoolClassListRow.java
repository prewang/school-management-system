package com.school.mapper.row;

import lombok.Data;

@Data
public class SchoolClassListRow {
    private Long id;
    private String name;
    private String grade;
    private Integer year;
    private Integer studentCount;
}
