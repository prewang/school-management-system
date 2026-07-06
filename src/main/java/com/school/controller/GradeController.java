package com.school.controller;

import com.school.common.request.PageRequest;
import com.school.common.result.PageResult;
import com.school.common.result.Result;
import com.school.dto.grade.GradeCreateRequest;
import com.school.dto.grade.GradePageResponse;
import com.school.dto.grade.GradeResponse;
import com.school.dto.grade.GradeUpdateRequest;
import com.school.service.GradeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "成绩管理")
@RestController
@RequestMapping("/grades")
@RequiredArgsConstructor
public class GradeController {

    private final GradeService gradeService;

    @Operation(summary = "录入成绩")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('TEACHER')")
    public Result<GradeResponse> create(@Valid @RequestBody GradeCreateRequest request) {
        return Result.success(gradeService.create(request));
    }

    @Operation(summary = "学生查询自己的成绩")
    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public Result<PageResult<GradePageResponse>> pageMy(@Valid PageRequest pageRequest,
                                                        @RequestParam(required = false) String semester) {
        return Result.success(gradeService.pageMy(pageRequest, semester));
    }

    @Operation(summary = "分页查询成绩")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('TEACHER')")
    public Result<PageResult<GradePageResponse>> page(@Valid PageRequest pageRequest,
                                                      @RequestParam(required = false) Long studentId,
                                                      @RequestParam(required = false) Long courseId,
                                                      @RequestParam(required = false) Long classId,
                                                      @RequestParam(required = false) String semester) {
        return Result.success(gradeService.page(pageRequest, studentId, courseId, classId, semester));
    }

    @Operation(summary = "更新成绩")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public Result<GradeResponse> update(@PathVariable Long id, @Valid @RequestBody GradeUpdateRequest request) {
        return Result.success(gradeService.update(id, request));
    }
}
