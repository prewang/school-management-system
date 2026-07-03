package com.school.service;

import com.school.common.request.PageRequest;
import com.school.common.result.PageResult;
import com.school.dto.student.StudentCreateRequest;
import com.school.dto.student.StudentPageResponse;
import com.school.dto.student.StudentResponse;
import com.school.dto.student.StudentUpdateRequest;

public interface StudentService {
    StudentResponse create(StudentCreateRequest request);
    StudentResponse getById(Long id);
    PageResult<StudentPageResponse> page(PageRequest pageRequest, Long classId, String keyword);
    StudentResponse update(Long id, StudentUpdateRequest request);
    void delete(Long id);
}
