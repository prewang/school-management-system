package com.school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("course_teacher")
public class CourseTeacher {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long courseId;

    private Long teacherId;
}
