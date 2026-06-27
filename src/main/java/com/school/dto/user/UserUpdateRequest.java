package com.school.dto.user;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateRequest {
    @Size(min = 6, max = 100)
    private String password;
    @Pattern(regexp = "SUPER_ADMIN|ADMIN|TEACHER|STUDENT", message = "角色值非法")
    private String role;
    private Integer status;
}
