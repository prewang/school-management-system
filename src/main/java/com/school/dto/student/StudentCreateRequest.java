package com.school.dto.student;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class StudentCreateRequest {
    @NotNull(message = "账号 ID 不能为空")
    private Long userId;
    @NotNull(message = "班级 ID 不能为空")
    private Long classId;
    @NotBlank(message = "学号不能为空")
    private String studentNo;
    private Integer gender;
    private LocalDate birthDate;
}
