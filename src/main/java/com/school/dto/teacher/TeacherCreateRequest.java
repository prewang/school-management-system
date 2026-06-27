package com.school.dto.teacher;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TeacherCreateRequest {
    @NotNull(message = "账号 ID 不能为空")
    private Long userId;
    @NotBlank(message = "工号不能为空")
    private String teacherNo;
    @NotBlank(message = "姓名不能为空")
    private String name;
    private Integer gender;
}
