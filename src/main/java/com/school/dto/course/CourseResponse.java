package com.school.dto.course;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CourseResponse {
    private Long id;
    private String name;
    private String code;
    private Integer credit;
    private LocalDateTime createTime;
}
