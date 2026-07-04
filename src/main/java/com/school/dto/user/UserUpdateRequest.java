package com.school.dto.user;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateRequest {

    @NotBlank(message = "真实姓名不能为空")
    @Size(max = 100)
    private String realName;

    @NotBlank(message = "角色不能为空")
    @Pattern(regexp = "ADMIN|TEACHER|STUDENT", message = "角色值非法")
    private String role;

    @NotNull(message = "状态不能为空")
    @Min(value = 0, message = "状态值非法，仅允许 0（禁用）或 1（启用）")
    @Max(value = 1, message = "状态值非法，仅允许 0（禁用）或 1（启用）")
    private Integer status;
}
