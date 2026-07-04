package com.school.dto.student;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.time.LocalDate;

@Data
public class StudentUpdateRequest {
    private Long classId;
    @Min(value = 0, message = "性别取值无效")
    @Max(value = 2, message = "性别取值无效")
    private Integer gender;
    private LocalDate birthDate;
}
