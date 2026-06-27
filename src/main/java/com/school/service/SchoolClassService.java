package com.school.service;

import com.school.common.request.PageRequest;
import com.school.common.result.PageResult;
import com.school.dto.schoolclass.SchoolClassCreateRequest;
import com.school.dto.schoolclass.SchoolClassPageResponse;
import com.school.dto.schoolclass.SchoolClassResponse;
import com.school.dto.schoolclass.SchoolClassUpdateRequest;

public interface SchoolClassService {
    SchoolClassResponse create(SchoolClassCreateRequest request);
    SchoolClassResponse getById(Long id);
    PageResult<SchoolClassPageResponse> page(PageRequest pageRequest);
    SchoolClassResponse update(Long id, SchoolClassUpdateRequest request);
    void delete(Long id);
}
