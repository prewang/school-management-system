package com.school.service;

import com.school.common.request.PageRequest;
import com.school.common.result.PageResult;
import com.school.dto.course.CourseCreateRequest;
import com.school.dto.course.CoursePageResponse;
import com.school.dto.course.CourseResponse;
import com.school.dto.course.CourseUpdateRequest;

public interface CourseService {
    CourseResponse create(CourseCreateRequest request);
    CourseResponse getById(Long id);
    PageResult<CoursePageResponse> page(PageRequest pageRequest);
    CourseResponse update(Long id, CourseUpdateRequest request);
    void delete(Long id);
}
