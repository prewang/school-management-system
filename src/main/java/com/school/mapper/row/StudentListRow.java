package com.school.mapper.row;

import lombok.Data;

@Data
public class StudentListRow {
    private Long id;
    private String studentNo;
    private String realName;
    private Integer gender;
    private Long classId;
    private String className;
}
