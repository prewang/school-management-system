package com.school.controller;

import com.school.common.request.PageRequest;
import com.school.common.result.PageResult;
import com.school.common.result.Result;
import com.school.dto.student.*;
import com.school.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "学生管理")
@RestController
@RequestMapping("/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @Operation(summary = "创建学生")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public Result<StudentResponse> create(@Valid @RequestBody StudentCreateRequest request) {
        return Result.success(studentService.create(request));
    }

    @Operation(summary = "获取学生详情")
    @GetMapping("/{id}")
    public Result<StudentResponse> getById(@PathVariable Long id) {
        return Result.success(studentService.getById(id));
    }

    @Operation(summary = "分页查询学生")
    @GetMapping
    public Result<PageResult<StudentPageResponse>> page(@Valid PageRequest pageRequest,
                                                        @RequestParam(required = false) Long classId) {
        return Result.success(studentService.page(pageRequest, classId));
    }

    @Operation(summary = "更新学生")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public Result<StudentResponse> update(@PathVariable Long id, @Valid @RequestBody StudentUpdateRequest request) {
        return Result.success(studentService.update(id, request));
    }

    @Operation(summary = "删除学生")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        studentService.delete(id);
        return Result.success();
    }
}
