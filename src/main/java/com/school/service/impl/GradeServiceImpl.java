package com.school.service.impl;

import com.school.common.request.PageRequest;
import com.school.common.result.PageResult;
import com.school.dto.grade.*;
import com.school.service.GradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GradeServiceImpl implements GradeService {
    @Override public GradeResponse create(GradeCreateRequest r) { throw new UnsupportedOperationException("TODO"); }
    @Override public GradeResponse getById(Long id) { throw new UnsupportedOperationException("TODO"); }
    @Override public PageResult<GradePageResponse> page(PageRequest p, Long sid, Long cid) { throw new UnsupportedOperationException("TODO"); }
    @Override public GradeResponse update(Long id, GradeUpdateRequest r) { throw new UnsupportedOperationException("TODO"); }
    @Override public void delete(Long id) { throw new UnsupportedOperationException("TODO"); }
}
