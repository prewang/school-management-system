package com.school.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.school.common.enums.ErrorCode;
import com.school.common.enums.Role;
import com.school.common.exception.BusinessException;
import com.school.common.request.PageRequest;
import com.school.common.result.PageResult;
import com.school.dto.student.*;
import com.school.entity.Grade;
import com.school.entity.SchoolClass;
import com.school.entity.Student;
import com.school.entity.SysUser;
import com.school.mapper.GradeMapper;
import com.school.mapper.SchoolClassMapper;
import com.school.mapper.StudentMapper;
import com.school.mapper.SysUserMapper;
import com.school.mapper.row.StudentDetailRow;
import com.school.mapper.row.StudentListRow;
import com.school.security.UserPrincipal;
import com.school.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final StudentMapper studentMapper;
    private final SysUserMapper sysUserMapper;
    private final SchoolClassMapper schoolClassMapper;
    private final GradeMapper gradeMapper;

    @Override
    @Transactional
    public StudentResponse create(StudentCreateRequest request) {
        if (studentMapper.countByStudentNo(request.getStudentNo()) > 0) {
            throw new BusinessException(ErrorCode.DATA_DUPLICATE, "学号已存在");
        }

        SysUser user = sysUserMapper.selectById(request.getUserId());
        if (user == null || !Role.STUDENT.name().equals(user.getRole())
                || !Integer.valueOf(1).equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "用户不存在");
        }

        if (studentMapper.countByUserId(request.getUserId()) > 0) {
            throw new BusinessException(ErrorCode.DATA_DUPLICATE, "该用户已有学生档案");
        }

        SchoolClass schoolClass = schoolClassMapper.selectById(request.getClassId());
        if (schoolClass == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "班级不存在");
        }

        Student student = new Student();
        student.setUserId(request.getUserId());
        student.setClassId(request.getClassId());
        student.setStudentNo(request.getStudentNo());
        student.setGender(request.getGender() != null ? request.getGender() : 2);
        student.setBirthDate(request.getBirthDate());
        studentMapper.insert(student);

        return toStudentResponse(student, user, schoolClass);
    }

    @Override
    @Transactional(readOnly = true)
    public StudentResponse getById(Long id) {
        StudentDetailRow row = studentMapper.selectStudentDetailById(id);
        if (row == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "资源不存在");
        }
        assertCanViewStudent(row.getUserId());
        return toStudentResponse(row);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<StudentPageResponse> page(PageRequest pageRequest, Long classId, String keyword) {
        Page<StudentListRow> mpPage = new Page<>(pageRequest.getPage(), pageRequest.getSize());
        IPage<StudentListRow> result = studentMapper.selectStudentPage(mpPage, classId, keyword);
        IPage<StudentPageResponse> dtoPage = result.convert(this::toStudentPageResponse);
        return PageResult.of(dtoPage);
    }

    @Override
    @Transactional
    public StudentResponse update(Long id, StudentUpdateRequest request) {
        if (request.getClassId() == null && request.getGender() == null && request.getBirthDate() == null) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "参数校验失败");
        }

        Student student = studentMapper.selectById(id);
        if (student == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
        }

        SchoolClass schoolClass = null;
        if (request.getClassId() != null) {
            schoolClass = schoolClassMapper.selectById(request.getClassId());
            if (schoolClass == null) {
                throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "班级不存在");
            }
            student.setClassId(request.getClassId());
        }
        if (request.getGender() != null) {
            student.setGender(request.getGender());
        }
        if (request.getBirthDate() != null) {
            student.setBirthDate(request.getBirthDate());
        }

        studentMapper.updateById(student);

        SysUser user = sysUserMapper.selectById(student.getUserId());
        if (user == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "用户不存在");
        }
        if (schoolClass == null) {
            schoolClass = schoolClassMapper.selectById(student.getClassId());
        }
        if (schoolClass == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "班级不存在");
        }
        return toStudentResponse(student, user, schoolClass);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Student student = studentMapper.selectById(id);
        if (student == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
        }

        boolean hasGrades = gradeMapper.exists(
                new LambdaQueryWrapper<Grade>().eq(Grade::getStudentId, id));
        if (hasGrades) {
            throw new BusinessException(ErrorCode.RELATION_EXISTS, "该学生存在成绩记录，无法删除");
        }

        studentMapper.deleteById(id);
    }

    private StudentResponse toStudentResponse(Student student, SysUser user, SchoolClass schoolClass) {
        StudentResponse response = new StudentResponse();
        response.setId(student.getId());
        response.setUserId(student.getUserId());
        response.setClassId(student.getClassId());
        response.setStudentNo(student.getStudentNo());
        response.setRealName(user.getRealName());
        response.setGender(student.getGender());
        response.setBirthDate(student.getBirthDate());
        response.setClassName(schoolClass.getName());
        response.setCreateTime(student.getCreateTime());
        return response;
    }

    private StudentPageResponse toStudentPageResponse(StudentListRow row) {
        StudentPageResponse response = new StudentPageResponse();
        response.setId(row.getId());
        response.setStudentNo(row.getStudentNo());
        response.setRealName(row.getRealName());
        response.setGender(row.getGender());
        response.setClassId(row.getClassId());
        response.setClassName(row.getClassName());
        return response;
    }

    private StudentResponse toStudentResponse(StudentDetailRow row) {
        StudentResponse response = new StudentResponse();
        response.setId(row.getId());
        response.setUserId(row.getUserId());
        response.setClassId(row.getClassId());
        response.setStudentNo(row.getStudentNo());
        response.setRealName(row.getRealName());
        response.setGender(row.getGender());
        response.setBirthDate(row.getBirthDate());
        response.setClassName(row.getClassName());
        response.setCreateTime(row.getCreateTime());
        return response;
    }

    private void assertCanViewStudent(Long studentUserId) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        if (hasRole(principal, Role.ADMIN) || hasRole(principal, Role.SUPER_ADMIN) || hasRole(principal, Role.TEACHER)) {
            return;
        }
        if (hasRole(principal, Role.STUDENT)) {
            if (!principal.getId().equals(studentUserId)) {
                throw new AccessDeniedException("无权查看该学生档案");
            }
            return;
        }
        throw new AccessDeniedException("无权查看该学生档案");
    }

    private boolean hasRole(UserPrincipal principal, Role role) {
        return principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role.name()));
    }
}
