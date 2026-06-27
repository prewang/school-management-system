package com.school.dto.student;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class StudentResponse {
    private Long id;
    private Long userId;
    private Long classId;
    private String studentNo;
    private String name;
    private Integer gender;
    private LocalDateTime createTime;
}
