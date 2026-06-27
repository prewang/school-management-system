package com.school.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("student")
public class Student extends BaseEntity {

    private Long userId;

    private Long classId;

    private String studentNo;

    private String name;

    /** 0=女，1=男，2=未知 */
    private Integer gender;
}
