package com.school.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("teacher")
public class Teacher extends BaseEntity {

    private Long userId;

    private String teacherNo;

    private String name;

    /** 0=女，1=男，2=未知 */
    private Integer gender;
}
