package com.school.controller;

import com.school.common.request.PageRequest;
import com.school.common.result.PageResult;
import com.school.common.result.Result;
import com.school.dto.course.CourseCreateRequest;
import com.school.dto.course.CoursePageResponse;
import com.school.dto.course.CourseResponse;
import com.school.dto.course.CourseTeacherAssignRequest;
import com.school.dto.course.CourseUpdateRequest;
import com.school.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "课程管理")
@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @Operation(summary = "创建课程")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public Result<CourseResponse> create(@Valid @RequestBody CourseCreateRequest request) {
        return Result.success(courseService.create(request));
    }

    @Operation(summary = "分页查询课程")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public Result<PageResult<CoursePageResponse>> page(@Valid PageRequest pageRequest,
                                                       @RequestParam(required = false) String keyword) {
        return Result.success(courseService.page(pageRequest, keyword));
    }

    @Operation(summary = "教师查询自己的课程")
    @GetMapping("/my")
    @PreAuthorize("hasRole('TEACHER')")
    public Result<PageResult<CoursePageResponse>> pageMy(@Valid PageRequest pageRequest,
                                                         @RequestParam(required = false) String keyword) {
        return Result.success(courseService.pageMy(pageRequest, keyword));
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

    @Operation(summary = "分配课程教师")
    @PostMapping("/{id}/teachers")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public Result<Void> assignTeachers(@PathVariable Long id,
                                       @Valid @RequestBody CourseTeacherAssignRequest request) {
        courseService.assignTeachers(id, request);
        return Result.success();
    }

    @Operation(summary = "移除课程教师关联")
    @DeleteMapping("/{id}/teachers/{teacherId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public Result<Void> removeTeacher(@PathVariable Long id, @PathVariable Long teacherId) {
        courseService.removeTeacher(id, teacherId);
        return Result.success();
    }
}
