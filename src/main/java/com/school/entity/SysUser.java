package com.school.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {

    private String username;

    private String passwordHash;

    /** SUPER_ADMIN / ADMIN / TEACHER / STUDENT */
    private String role;

    /** 0=禁用，1=启用 */
    private Integer status;
}
