package com.school.dto.course;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CourseTeacherAssignRequest {

    @NotEmpty(message = "教师 ID 列表不能为空")
    private List<Long> teacherIds;
}
