package com.school.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.school.common.enums.ErrorCode;
import com.school.common.enums.Role;
import com.school.common.exception.BusinessException;
import com.school.common.request.PageRequest;
import com.school.common.result.PageResult;
import com.school.dto.teacher.*;
import com.school.entity.CourseTeacher;
import com.school.entity.SysUser;
import com.school.entity.Teacher;
import com.school.mapper.CourseTeacherMapper;
import com.school.mapper.SysUserMapper;
import com.school.mapper.TeacherMapper;
import com.school.mapper.row.TeacherDetailRow;
import com.school.mapper.row.TeacherListRow;
import com.school.security.UserPrincipal;
import com.school.service.TeacherService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TeacherServiceImpl implements TeacherService {

    private final TeacherMapper teacherMapper;
    private final SysUserMapper sysUserMapper;
    private final CourseTeacherMapper courseTeacherMapper;

    @Override
    @Transactional
    public TeacherResponse create(TeacherCreateRequest request) {
        if (teacherMapper.countByTeacherNo(request.getTeacherNo()) > 0) {
            throw new BusinessException(ErrorCode.DATA_DUPLICATE, "工号已存在");
        }

        SysUser user = sysUserMapper.selectById(request.getUserId());
        if (user == null || !Role.TEACHER.name().equals(user.getRole())
                || !Integer.valueOf(1).equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "用户不存在");
        }

        if (teacherMapper.countByUserId(request.getUserId()) > 0) {
            throw new BusinessException(ErrorCode.DATA_DUPLICATE, "该用户已有教师档案");
        }

        Teacher teacher = new Teacher();
        teacher.setUserId(request.getUserId());
        teacher.setTeacherNo(request.getTeacherNo());
        teacher.setDepartment(request.getDepartment());
        teacherMapper.insert(teacher);

        return toTeacherResponse(teacher, user, Collections.emptyList());
    }

    @Override
    @Transactional(readOnly = true)
    public TeacherResponse getById(Long id) {
        TeacherDetailRow row = teacherMapper.selectTeacherDetailById(id);
        if (row == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "资源不存在");
        }
        assertCanViewTeacher(row.getUserId());
        List<String> courseNames = teacherMapper.selectCourseNamesByTeacherId(id);
        return toTeacherResponse(row, courseNames);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<TeacherPageResponse> page(PageRequest pageRequest, String department, String keyword) {
        Page<TeacherListRow> mpPage = new Page<>(pageRequest.getPage(), pageRequest.getSize());
        IPage<TeacherListRow> result = teacherMapper.selectTeacherPage(mpPage, department, keyword);
        IPage<TeacherPageResponse> dtoPage = result.convert(this::toTeacherPageResponse);
        return PageResult.of(dtoPage);
    }

    @Override
    @Transactional
    public TeacherResponse update(Long id, TeacherUpdateRequest request) {
        if (request.getDepartment() == null) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "参数校验失败");
        }

        Teacher teacher = teacherMapper.selectById(id);
        if (teacher == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
        }

        teacher.setDepartment(request.getDepartment());
        teacherMapper.updateById(teacher);

        SysUser user = sysUserMapper.selectById(teacher.getUserId());
        if (user == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "用户不存在");
        }

        List<String> courseNames = teacherMapper.selectCourseNamesByTeacherId(id);
        return toTeacherResponse(teacher, user, courseNames);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Teacher teacher = teacherMapper.selectById(id);
        if (teacher == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
        }

        boolean hasCourses = courseTeacherMapper.exists(
                new LambdaQueryWrapper<CourseTeacher>().eq(CourseTeacher::getTeacherId, id));
        if (hasCourses) {
            throw new BusinessException(ErrorCode.RELATION_EXISTS, "该教师仍有关联课程，请先移除课程关联");
        }

        teacherMapper.deleteById(id);
    }

    private TeacherPageResponse toTeacherPageResponse(TeacherListRow row) {
        TeacherPageResponse response = new TeacherPageResponse();
        response.setId(row.getId());
        response.setTeacherNo(row.getTeacherNo());
        response.setRealName(row.getRealName());
        response.setDepartment(row.getDepartment());
        return response;
    }

    private TeacherResponse toTeacherResponse(Teacher teacher, SysUser user, List<String> courseNames) {
        TeacherResponse response = new TeacherResponse();
        response.setId(teacher.getId());
        response.setUserId(teacher.getUserId());
        response.setTeacherNo(teacher.getTeacherNo());
        response.setRealName(user.getRealName());
        response.setDepartment(teacher.getDepartment());
        response.setCourseNames(courseNames);
        response.setCreateTime(teacher.getCreateTime());
        return response;
    }

    private TeacherResponse toTeacherResponse(TeacherDetailRow row, List<String> courseNames) {
        TeacherResponse response = new TeacherResponse();
        response.setId(row.getId());
        response.setUserId(row.getUserId());
        response.setTeacherNo(row.getTeacherNo());
        response.setRealName(row.getRealName());
        response.setDepartment(row.getDepartment());
        response.setCourseNames(courseNames);
        response.setCreateTime(row.getCreateTime());
        return response;
    }

    private void assertCanViewTeacher(Long teacherUserId) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        if (hasRole(principal, Role.ADMIN) || hasRole(principal, Role.SUPER_ADMIN)) {
            return;
        }
        if (hasRole(principal, Role.TEACHER)) {
            if (!principal.getId().equals(teacherUserId)) {
                throw new AccessDeniedException("无权查看该教师档案");
            }
            return;
        }
        throw new AccessDeniedException("无权查看该教师档案");
    }

    private boolean hasRole(UserPrincipal principal, Role role) {
        return principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role.name()));
    }
}
