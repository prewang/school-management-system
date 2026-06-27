package com.school.controller;

import com.school.common.request.PageRequest;
import com.school.common.result.PageResult;
import com.school.common.result.Result;
import com.school.dto.schoolclass.*;
import com.school.service.SchoolClassService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "班级管理")
@RestController
@RequestMapping("/classes")
@RequiredArgsConstructor
public class SchoolClassController {

    private final SchoolClassService schoolClassService;

    @Operation(summary = "创建班级")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public Result<SchoolClassResponse> create(@Valid @RequestBody SchoolClassCreateRequest request) {
        return Result.success(schoolClassService.create(request));
    }

    @Operation(summary = "获取班级详情")
    @GetMapping("/{id}")
    public Result<SchoolClassResponse> getById(@PathVariable Long id) {
        return Result.success(schoolClassService.getById(id));
    }

    @Operation(summary = "分页查询班级")
    @GetMapping
    public Result<PageResult<SchoolClassPageResponse>> page(@Valid PageRequest pageRequest) {
        return Result.success(schoolClassService.page(pageRequest));
    }

    @Operation(summary = "更新班级")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public Result<SchoolClassResponse> update(@PathVariable Long id, @Valid @RequestBody SchoolClassUpdateRequest request) {
        return Result.success(schoolClassService.update(id, request));
    }

    @Operation(summary = "删除班级")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        schoolClassService.delete(id);
        return Result.success();
    }
}
