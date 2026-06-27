package com.school.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("class")
public class SchoolClass extends BaseEntity {

    private String name;

    private String grade;

    private Integer year;
}
