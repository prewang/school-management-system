package com.school.dto.schoolclass;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SchoolClassResponse {
    private Long id;
    private String name;
    private String grade;
    private Integer year;
    private LocalDateTime createTime;
}
