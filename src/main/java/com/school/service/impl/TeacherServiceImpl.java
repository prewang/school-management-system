package com.school.service.impl;

import com.school.common.request.PageRequest;
import com.school.common.result.PageResult;
import com.school.dto.teacher.*;
import com.school.service.TeacherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TeacherServiceImpl implements TeacherService {
    @Override public TeacherResponse create(TeacherCreateRequest r) { throw new UnsupportedOperationException("TODO"); }
    @Override public TeacherResponse getById(Long id) { throw new UnsupportedOperationException("TODO"); }
    @Override public PageResult<TeacherPageResponse> page(PageRequest p) { throw new UnsupportedOperationException("TODO"); }
    @Override public TeacherResponse update(Long id, TeacherUpdateRequest r) { throw new UnsupportedOperationException("TODO"); }
    @Override public void delete(Long id) { throw new UnsupportedOperationException("TODO"); }
}
