package com.school.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserCreateRequest {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50)
    private String username;
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100)
    private String password;
    @NotBlank(message = "角色不能为空")
    @Pattern(regexp = "SUPER_ADMIN|ADMIN|TEACHER|STUDENT", message = "角色值非法")
    private String role;
}
