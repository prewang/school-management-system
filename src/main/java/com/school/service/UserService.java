package com.school.service;

import com.school.common.request.PageRequest;
import com.school.common.result.PageResult;
import com.school.dto.user.PasswordUpdateRequest;
import com.school.dto.user.UserCreateRequest;
import com.school.dto.user.UserPageResponse;
import com.school.dto.user.UserResponse;
import com.school.dto.user.UserUpdateRequest;

public interface UserService {
    UserResponse create(UserCreateRequest request);
    UserResponse getById(Long id);
    PageResult<UserPageResponse> page(PageRequest pageRequest, String role, String keyword);
    UserResponse update(Long id, UserUpdateRequest request);
    void delete(Long id);
    void updatePassword(PasswordUpdateRequest request);
}
