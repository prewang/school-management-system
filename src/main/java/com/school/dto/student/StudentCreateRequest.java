package com.school.dto.student;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class StudentCreateRequest {
    @NotNull(message = "账号 ID 不能为空")
    private Long userId;
    @NotNull(message = "班级 ID 不能为空")
    private Long classId;
    @NotBlank(message = "学号不能为空")
    @Size(max = 50, message = "学号长度不能超过 50 个字符")
    private String studentNo;
    @Min(value = 0, message = "性别取值无效")
    @Max(value = 2, message = "性别取值无效")
    private Integer gender;
    private LocalDate birthDate;
}
