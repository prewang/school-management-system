package com.school.dto.teacher;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TeacherResponse {
    private Long id;
    private Long userId;
    private String teacherNo;
    private String name;
    private Integer gender;
    private LocalDateTime createTime;
}
