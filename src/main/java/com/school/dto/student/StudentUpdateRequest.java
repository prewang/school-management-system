package com.school.dto.student;

import lombok.Data;

import java.time.LocalDate;

@Data
public class StudentUpdateRequest {
    private Long classId;
    private Integer gender;
    private LocalDate birthDate;
}
