package com.school.service.impl;

import com.school.common.request.PageRequest;
import com.school.common.result.PageResult;
import com.school.dto.student.*;
import com.school.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {
    @Override public StudentResponse create(StudentCreateRequest r) { throw new UnsupportedOperationException("TODO"); }
    @Override public StudentResponse getById(Long id) { throw new UnsupportedOperationException("TODO"); }
    @Override public PageResult<StudentPageResponse> page(PageRequest p, Long classId) { throw new UnsupportedOperationException("TODO"); }
    @Override public StudentResponse update(Long id, StudentUpdateRequest r) { throw new UnsupportedOperationException("TODO"); }
    @Override public void delete(Long id) { throw new UnsupportedOperationException("TODO"); }
}
