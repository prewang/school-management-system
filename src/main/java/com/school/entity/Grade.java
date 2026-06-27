package com.school.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("grade")
public class Grade extends BaseEntity {

    private Long studentId;

    private Long courseId;

    /** 0.00 ~ 100.00 */
    private BigDecimal score;

    /** 格式 YYYY-S，如 2024-1、2024-2 */
    private String semester;
}
