package com.school.service.impl;

import com.school.common.request.PageRequest;
import com.school.common.result.PageResult;
import com.school.dto.course.*;
import com.school.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {
    @Override public CourseResponse create(CourseCreateRequest r) { throw new UnsupportedOperationException("TODO"); }
    @Override public CourseResponse getById(Long id) { throw new UnsupportedOperationException("TODO"); }
    @Override public PageResult<CoursePageResponse> page(PageRequest p) { throw new UnsupportedOperationException("TODO"); }
    @Override public CourseResponse update(Long id, CourseUpdateRequest r) { throw new UnsupportedOperationException("TODO"); }
    @Override public void delete(Long id) { throw new UnsupportedOperationException("TODO"); }
}
