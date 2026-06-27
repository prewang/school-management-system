package com.school.controller;

import com.school.common.request.PageRequest;
import com.school.common.result.PageResult;
import com.school.common.result.Result;
import com.school.dto.user.*;
import com.school.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户管理")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "创建用户")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public Result<UserResponse> create(@Valid @RequestBody UserCreateRequest request) {
        return Result.success(userService.create(request));
    }

    @Operation(summary = "获取用户详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public Result<UserResponse> getById(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    @Operation(summary = "分页查询用户")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public Result<PageResult<UserPageResponse>> page(@Valid PageRequest pageRequest,
                                                     @RequestParam(required = false) String role) {
        return Result.success(userService.page(pageRequest, role));
    }

    @Operation(summary = "更新用户")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public Result<UserResponse> update(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        return Result.success(userService.update(id, request));
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return Result.success();
    }
}
