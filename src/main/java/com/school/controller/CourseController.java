package com.school.controller;

import com.school.common.request.PageRequest;
import com.school.common.result.PageResult;
import com.school.common.result.Result;
import com.school.dto.course.*;
import com.school.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "课程管理")
@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @Operation(summary = "创建课程")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public Result<CourseResponse> create(@Valid @RequestBody CourseCreateRequest request) {
        return Result.success(courseService.create(request));
    }

    @Operation(summary = "获取课程详情")
    @GetMapping("/{id}")
    public Result<CourseResponse> getById(@PathVariable Long id) {
        return Result.success(courseService.getById(id));
    }

    @Operation(summary = "分页查询课程")
    @GetMapping
    public Result<PageResult<CoursePageResponse>> page(@Valid PageRequest pageRequest) {
        return Result.success(courseService.page(pageRequest));
    }

    @Operation(summary = "更新课程")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public Result<CourseResponse> update(@PathVariable Long id, @Valid @RequestBody CourseUpdateRequest request) {
        return Result.success(courseService.update(id, request));
    }

    @Operation(summary = "删除课程")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        courseService.delete(id);
        return Result.success();
    }
}
