package com.school.dto.user;

import lombok.Data;

@Data
public class UserPageResponse {
    private Long id;
    private String username;
    private String realName;
    private String role;
    private Integer status;
}
