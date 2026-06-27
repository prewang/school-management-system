package com.school.service;

import com.school.common.request.PageRequest;
import com.school.common.result.PageResult;
import com.school.dto.grade.GradeCreateRequest;
import com.school.dto.grade.GradePageResponse;
import com.school.dto.grade.GradeResponse;
import com.school.dto.grade.GradeUpdateRequest;

public interface GradeService {
    GradeResponse create(GradeCreateRequest request);
    GradeResponse getById(Long id);
    PageResult<GradePageResponse> page(PageRequest pageRequest, Long studentId, Long courseId);
    GradeResponse update(Long id, GradeUpdateRequest request);
    void delete(Long id);
}
