package com.school.service;

import com.school.common.request.PageRequest;
import com.school.common.result.PageResult;
import com.school.dto.course.CourseCreateRequest;
import com.school.dto.course.CoursePageResponse;
import com.school.dto.course.CourseResponse;
import com.school.dto.course.CourseTeacherAssignRequest;
import com.school.dto.course.CourseUpdateRequest;

public interface CourseService {

    CourseResponse create(CourseCreateRequest request);

    PageResult<CoursePageResponse> page(PageRequest pageRequest, String keyword);

    PageResult<CoursePageResponse> pageMy(PageRequest pageRequest, String keyword);

    CourseResponse update(Long id, CourseUpdateRequest request);

    void delete(Long id);

    void assignTeachers(Long courseId, CourseTeacherAssignRequest request);

    void removeTeacher(Long courseId, Long teacherId);
}
