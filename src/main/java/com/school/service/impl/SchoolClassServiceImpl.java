package com.school.service.impl;

import com.school.common.request.PageRequest;
import com.school.common.result.PageResult;
import com.school.dto.schoolclass.*;
import com.school.service.SchoolClassService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SchoolClassServiceImpl implements SchoolClassService {
    @Override public SchoolClassResponse create(SchoolClassCreateRequest r) { throw new UnsupportedOperationException("TODO"); }
    @Override public SchoolClassResponse getById(Long id) { throw new UnsupportedOperationException("TODO"); }
    @Override public PageResult<SchoolClassPageResponse> page(PageRequest p) { throw new UnsupportedOperationException("TODO"); }
    @Override public SchoolClassResponse update(Long id, SchoolClassUpdateRequest r) { throw new UnsupportedOperationException("TODO"); }
    @Override public void delete(Long id) { throw new UnsupportedOperationException("TODO"); }
}
