package com.school.controller;

import com.school.common.request.PageRequest;
import com.school.common.result.PageResult;
import com.school.common.result.Result;
import com.school.dto.grade.*;
import com.school.service.GradeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "成绩管理")
@RestController
@RequestMapping("/grades")
@RequiredArgsConstructor
public class GradeController {

    private final GradeService gradeService;

    @Operation(summary = "录入成绩")
    @PostMapping
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public Result<GradeResponse> create(@Valid @RequestBody GradeCreateRequest request) {
        return Result.success(gradeService.create(request));
    }

    @Operation(summary = "获取成绩详情")
    @GetMapping("/{id}")
    public Result<GradeResponse> getById(@PathVariable Long id) {
        return Result.success(gradeService.getById(id));
    }

    @Operation(summary = "分页查询成绩")
    @GetMapping
    public Result<PageResult<GradePageResponse>> page(@Valid PageRequest pageRequest,
                                                      @RequestParam(required = false) Long studentId,
                                                      @RequestParam(required = false) Long courseId) {
        return Result.success(gradeService.page(pageRequest, studentId, courseId));
    }

    @Operation(summary = "更新成绩")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public Result<GradeResponse> update(@PathVariable Long id, @Valid @RequestBody GradeUpdateRequest request) {
        return Result.success(gradeService.update(id, request));
    }

    @Operation(summary = "删除成绩")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        gradeService.delete(id);
        return Result.success();
    }
}
