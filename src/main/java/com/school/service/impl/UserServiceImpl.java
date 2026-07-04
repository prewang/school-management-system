package com.school.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.school.common.enums.ErrorCode;
import com.school.common.enums.Role;
import com.school.common.exception.BusinessException;
import com.school.common.request.PageRequest;
import com.school.common.result.PageResult;
import com.school.entity.Student;
import com.school.entity.Teacher;
import com.school.dto.user.PasswordUpdateRequest;
import com.school.dto.user.UserCreateRequest;
import com.school.dto.user.UserPageResponse;
import com.school.dto.user.UserResponse;
import com.school.dto.user.UserUpdateRequest;
import com.school.entity.SysUser;
import com.school.mapper.StudentMapper;
import com.school.mapper.SysUserMapper;
import com.school.mapper.TeacherMapper;
import com.school.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import com.school.security.UserPrincipal;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final StudentMapper studentMapper;
    private final TeacherMapper teacherMapper;

    @Override
    @Transactional
    public UserResponse create(UserCreateRequest request) {
        boolean exists = sysUserMapper.exists(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, request.getUsername()));
        if (exists) {
            throw new BusinessException(ErrorCode.DATA_DUPLICATE, "用户名已存在");
        }

        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setRole(request.getRole());
        user.setStatus(1);
        sysUserMapper.insert(user);

        return toUserResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
        }
        return toUserResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<UserPageResponse> page(PageRequest pageRequest, String role, String keyword) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(role), SysUser::getRole, role);
        wrapper.like(StringUtils.hasText(keyword), SysUser::getUsername, keyword);

        Page<SysUser> mpPage = new Page<>(pageRequest.getPage(), pageRequest.getSize());
        IPage<SysUser> result = sysUserMapper.selectPage(mpPage, wrapper);

        IPage<UserPageResponse> dtoPage = result.convert(this::toUserPageResponse);

        return PageResult.of(dtoPage);
    }

    @Override
    @Transactional
    public UserResponse update(Long id, UserUpdateRequest request) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
        }
        assertCanManageUser(user);

        // MVP：仅更新 sys_user，不联动 teacher/student 档案（角色变更时档案数据保留）
        user.setRealName(request.getRealName());
        user.setRole(request.getRole());
        user.setStatus(request.getStatus());
        sysUserMapper.updateById(user);

        return toUserResponse(user);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
        }
        assertCanManageUser(user);

        Long currentUserId = currentUserId();
        if (id != null && id.equals(currentUserId)) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "不能删除当前登录账号");
        }

        boolean hasStudent = studentMapper.exists(
                new LambdaQueryWrapper<Student>().eq(Student::getUserId, id)
        );
        if (hasStudent) {
            throw new BusinessException(ErrorCode.RELATION_EXISTS);
        }

        boolean hasTeacher = teacherMapper.exists(
                new LambdaQueryWrapper<Teacher>().eq(Teacher::getUserId, id)
        );
        if (hasTeacher) {
            throw new BusinessException(ErrorCode.RELATION_EXISTS);
        }

        // MyBatis-Plus logic delete will set `deleted=1` when `logic-delete` is enabled.
        sysUserMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void updatePassword(PasswordUpdateRequest request) {
        Long userId = currentUserId();
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
        }
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.PASSWORD_INCORRECT, "原密码错误");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        sysUserMapper.updateById(user);
    }

    private UserResponse toUserResponse(SysUser user) {
        UserResponse dto = new UserResponse();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRealName(user.getRealName());
        dto.setRole(user.getRole());
        dto.setStatus(user.getStatus());
        dto.setCreateTime(user.getCreateTime());
        dto.setUpdateTime(user.getUpdateTime());
        return dto;
    }

    private UserPageResponse toUserPageResponse(SysUser user) {
        UserPageResponse dto = new UserPageResponse();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRealName(user.getRealName());
        dto.setRole(user.getRole());
        dto.setStatus(user.getStatus());
        return dto;
    }

    private Long currentUserId() {
        return currentPrincipal().getId();
    }

    private UserPrincipal currentPrincipal() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        if (!(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return principal;
    }

    /**
     * 仅 SUPER_ADMIN 可操作 SUPER_ADMIN 账号；ADMIN 不得修改或删除 SUPER_ADMIN。
     */
    private void assertCanManageUser(SysUser target) {
        if (!Role.SUPER_ADMIN.name().equals(target.getRole())) {
            return;
        }
        if (!hasRole(currentPrincipal(), Role.SUPER_ADMIN)) {
            throw new AccessDeniedException("无权操作超级管理员账号");
        }
    }

    private boolean hasRole(UserPrincipal principal, Role role) {
        return principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role.name()));
    }
}
