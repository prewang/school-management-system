package com.school.dto.schoolclass;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class SchoolClassResponse {
    private Long id;
    private String name;
    private String grade;
    private Integer year;
    private LocalDateTime createTime;
    private List<ClassStudentItem> students;
}
