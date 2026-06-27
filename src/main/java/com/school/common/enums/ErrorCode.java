package com.school.common.enums;

import lombok.Getter;

@Getter
public enum ErrorCode {

    PARAM_INVALID(40000, "参数校验失败"),
    USER_NOT_FOUND(40001, "用户名不存在"),
    PASSWORD_INCORRECT(40002, "密码错误"),
    ACCOUNT_DISABLED(40003, "账号已禁用"),
    RESOURCE_NOT_FOUND(40004, "资源不存在"),
    DATA_DUPLICATE(40005, "数据重复"),
    RELATION_EXISTS(40006, "关联数据存在，禁止删除"),
    UNAUTHORIZED(40101, "未登录或 Token 已过期"),
    FORBIDDEN(40301, "无权限访问"),
    INTERNAL_ERROR(50000, "服务器内部错误");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
