package com.school.service.impl;

import com.school.common.request.PageRequest;
import com.school.common.result.PageResult;
import com.school.dto.user.*;
import com.school.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    @Override public UserResponse create(UserCreateRequest r) { throw new UnsupportedOperationException("TODO"); }
    @Override public UserResponse getById(Long id) { throw new UnsupportedOperationException("TODO"); }
    @Override public PageResult<UserPageResponse> page(PageRequest p, String role) { throw new UnsupportedOperationException("TODO"); }
    @Override public UserResponse update(Long id, UserUpdateRequest r) { throw new UnsupportedOperationException("TODO"); }
    @Override public void delete(Long id) { throw new UnsupportedOperationException("TODO"); }
}
