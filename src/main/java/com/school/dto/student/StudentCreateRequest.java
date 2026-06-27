package com.school.dto.student;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StudentCreateRequest {
    @NotNull(message = "账号 ID 不能为空")
    private Long userId;
    @NotNull(message = "班级 ID 不能为空")
    private Long classId;
    @NotBlank(message = "学号不能为空")
    private String studentNo;
    @NotBlank(message = "姓名不能为空")
    private String name;
    private Integer gender;
}
