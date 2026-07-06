package com.school.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.school.common.enums.ErrorCode;
import com.school.common.exception.BusinessException;
import com.school.common.request.PageRequest;
import com.school.common.result.PageResult;
import com.school.dto.schoolclass.*;
import com.school.entity.SchoolClass;
import com.school.entity.Student;
import com.school.mapper.SchoolClassMapper;
import com.school.mapper.StudentMapper;
import com.school.mapper.row.ClassStudentRow;
import com.school.mapper.row.SchoolClassListRow;
import com.school.service.SchoolClassService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SchoolClassServiceImpl implements SchoolClassService {

    private final SchoolClassMapper schoolClassMapper;
    private final StudentMapper studentMapper;

    @Override
    @Transactional
    public SchoolClassResponse create(SchoolClassCreateRequest request) {
        assertNameYearUnique(request.getName(), request.getYear(), null);

        SchoolClass schoolClass = new SchoolClass();
        schoolClass.setName(request.getName());
        schoolClass.setGrade(request.getGrade());
        schoolClass.setYear(request.getYear());
        schoolClassMapper.insert(schoolClass);

        return toSchoolClassResponse(schoolClass);
    }

    @Override
    @Transactional(readOnly = true)
    public SchoolClassResponse getById(Long id) {
        SchoolClass schoolClass = schoolClassMapper.selectById(id);
        if (schoolClass == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "资源不存在");
        }

        List<ClassStudentRow> studentRows = schoolClassMapper.selectStudentsByClassId(id);
        return toSchoolClassDetailResponse(schoolClass, studentRows);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<SchoolClassPageResponse> page(PageRequest pageRequest, Integer year) {
        Page<SchoolClassListRow> mpPage = new Page<>(pageRequest.getPage(), pageRequest.getSize());
        IPage<SchoolClassListRow> result = schoolClassMapper.selectSchoolClassPage(mpPage, year);
        IPage<SchoolClassPageResponse> dtoPage = result.convert(this::toSchoolClassPageResponse);
        return PageResult.of(dtoPage);
    }

    @Override
    @Transactional
    public SchoolClassResponse update(Long id, SchoolClassUpdateRequest request) {
        if (request.getName() == null && request.getGrade() == null && request.getYear() == null) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "参数校验失败");
        }

        SchoolClass schoolClass = schoolClassMapper.selectById(id);
        if (schoolClass == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "资源不存在");
        }

        if (request.getName() != null) {
            schoolClass.setName(request.getName());
        }
        if (request.getGrade() != null) {
            schoolClass.setGrade(request.getGrade());
        }
        if (request.getYear() != null) {
            schoolClass.setYear(request.getYear());
        }

        assertNameYearUnique(schoolClass.getName(), schoolClass.getYear(), id);

        schoolClassMapper.updateById(schoolClass);
        return toSchoolClassResponse(schoolClass);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        SchoolClass schoolClass = schoolClassMapper.selectById(id);
        if (schoolClass == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "资源不存在");
        }

        boolean hasStudents = studentMapper.exists(
                new LambdaQueryWrapper<Student>().eq(Student::getClassId, id));
        if (hasStudents) {
            throw new BusinessException(ErrorCode.RELATION_EXISTS, "该班级下仍有学生，请先转移学生");
        }

        schoolClassMapper.deleteById(id);
    }

    private void assertNameYearUnique(String name, Integer year, Long excludeId) {
        if (schoolClassMapper.countByNameAndYear(name, year, excludeId) > 0) {
            throw new BusinessException(ErrorCode.DATA_DUPLICATE, "该年份下班级名称已存在");
        }
    }

    private SchoolClassResponse toSchoolClassResponse(SchoolClass schoolClass) {
        SchoolClassResponse response = new SchoolClassResponse();
        response.setId(schoolClass.getId());
        response.setName(schoolClass.getName());
        response.setGrade(schoolClass.getGrade());
        response.setYear(schoolClass.getYear());
        response.setCreateTime(schoolClass.getCreateTime());
        response.setStudents(Collections.emptyList());
        return response;
    }

    private SchoolClassResponse toSchoolClassDetailResponse(SchoolClass schoolClass, List<ClassStudentRow> studentRows) {
        SchoolClassResponse response = toSchoolClassResponse(schoolClass);
        if (studentRows == null || studentRows.isEmpty()) {
            return response;
        }
        response.setStudents(studentRows.stream().map(this::toClassStudentItem).toList());
        return response;
    }

    private ClassStudentItem toClassStudentItem(ClassStudentRow row) {
        ClassStudentItem item = new ClassStudentItem();
        item.setId(row.getId());
        item.setStudentNo(row.getStudentNo());
        item.setRealName(row.getRealName());
        item.setGender(row.getGender());
        return item;
    }

    private SchoolClassPageResponse toSchoolClassPageResponse(SchoolClassListRow row) {
        SchoolClassPageResponse response = new SchoolClassPageResponse();
        response.setId(row.getId());
        response.setName(row.getName());
        response.setGrade(row.getGrade());
        response.setYear(row.getYear());
        response.setStudentCount(row.getStudentCount() != null ? row.getStudentCount() : 0);
        return response;
    }
}
