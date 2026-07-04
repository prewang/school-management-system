package com.school.dto.teacher;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TeacherCreateRequest {
    @NotNull(message = "账号 ID 不能为空")
    private Long userId;
    @NotBlank(message = "工号不能为空")
    @Size(max = 50, message = "工号长度不能超过 50 个字符")
    private String teacherNo;
    @NotBlank(message = "院系不能为空")
    @Size(max = 100, message = "院系长度不能超过 100 个字符")
    private String department;
}
