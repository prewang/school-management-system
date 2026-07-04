package com.school.dto.teacher;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TeacherUpdateRequest {
    @Size(max = 100, message = "院系长度不能超过 100 个字符")
    private String department;
}
