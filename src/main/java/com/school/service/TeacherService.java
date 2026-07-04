package com.school.service;

import com.school.common.request.PageRequest;
import com.school.common.result.PageResult;
import com.school.dto.teacher.TeacherCreateRequest;
import com.school.dto.teacher.TeacherPageResponse;
import com.school.dto.teacher.TeacherResponse;
import com.school.dto.teacher.TeacherUpdateRequest;

public interface TeacherService {
    TeacherResponse create(TeacherCreateRequest request);
    TeacherResponse getById(Long id);
    PageResult<TeacherPageResponse> page(PageRequest pageRequest, String department, String keyword);
    TeacherResponse update(Long id, TeacherUpdateRequest request);
    void delete(Long id);
}
