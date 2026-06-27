package com.school.controller;

import com.school.common.request.PageRequest;
import com.school.common.result.PageResult;
import com.school.common.result.Result;
import com.school.dto.teacher.*;
import com.school.service.TeacherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "教师管理")
@RestController
@RequestMapping("/teachers")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;

    @Operation(summary = "创建教师")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public Result<TeacherResponse> create(@Valid @RequestBody TeacherCreateRequest request) {
        return Result.success(teacherService.create(request));
    }

    @Operation(summary = "获取教师详情")
    @GetMapping("/{id}")
    public Result<TeacherResponse> getById(@PathVariable Long id) {
        return Result.success(teacherService.getById(id));
    }

    @Operation(summary = "分页查询教师")
    @GetMapping
    public Result<PageResult<TeacherPageResponse>> page(@Valid PageRequest pageRequest) {
        return Result.success(teacherService.page(pageRequest));
    }

    @Operation(summary = "更新教师")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public Result<TeacherResponse> update(@PathVariable Long id, @Valid @RequestBody TeacherUpdateRequest request) {
        return Result.success(teacherService.update(id, request));
    }

    @Operation(summary = "删除教师")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        teacherService.delete(id);
        return Result.success();
    }
}
