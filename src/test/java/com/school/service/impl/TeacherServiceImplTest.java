package com.school.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.school.common.enums.ErrorCode;
import com.school.common.enums.Role;
import com.school.common.exception.BusinessException;
import com.school.dto.teacher.TeacherCreateRequest;
import com.school.dto.teacher.TeacherUpdateRequest;
import com.school.entity.SysUser;
import com.school.entity.Teacher;
import com.school.mapper.CourseTeacherMapper;
import com.school.mapper.SysUserMapper;
import com.school.mapper.TeacherMapper;
import com.school.mapper.row.TeacherDetailRow;
import com.school.security.UserPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeacherServiceImplTest {

    @Mock
    private TeacherMapper teacherMapper;

    @Mock
    private SysUserMapper sysUserMapper;

    @Mock
    private CourseTeacherMapper courseTeacherMapper;

    @InjectMocks
    private TeacherServiceImpl teacherService;

    private SysUser teacherUser;

    @BeforeEach
    void setUp() {
        teacherUser = new SysUser();
        teacherUser.setId(10L);
        teacherUser.setUsername("teacher1");
        teacherUser.setPasswordHash("$2a$10$hash");
        teacherUser.setRealName("张老师");
        teacherUser.setRole(Role.TEACHER.name());
        teacherUser.setStatus(1);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // --- create ---

    @Test
    void create_duplicateTeacherNo_throws40005() {
        setPrincipal(1L, Role.ADMIN);
        TeacherCreateRequest request = validCreateRequest();
        when(teacherMapper.countByTeacherNo("T001")).thenReturn(1);

        BusinessException ex = assertThrows(BusinessException.class, () -> teacherService.create(request));
        assertEquals(ErrorCode.DATA_DUPLICATE.getCode(), ex.getCode());
        assertEquals("工号已存在", ex.getMessage());
    }

    @Test
    void create_userNotFound_throws40004() {
        setPrincipal(1L, Role.ADMIN);
        TeacherCreateRequest request = validCreateRequest();
        when(teacherMapper.countByTeacherNo("T001")).thenReturn(0);
        when(sysUserMapper.selectById(10L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> teacherService.create(request));
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND.getCode(), ex.getCode());
        assertEquals("用户不存在", ex.getMessage());
    }

    @Test
    void create_userWrongRole_throws40004() {
        setPrincipal(1L, Role.ADMIN);
        TeacherCreateRequest request = validCreateRequest();
        teacherUser.setRole(Role.STUDENT.name());
        when(teacherMapper.countByTeacherNo("T001")).thenReturn(0);
        when(sysUserMapper.selectById(10L)).thenReturn(teacherUser);

        BusinessException ex = assertThrows(BusinessException.class, () -> teacherService.create(request));
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND.getCode(), ex.getCode());
        assertEquals("用户不存在", ex.getMessage());
    }

    @Test
    void create_userDisabled_throws40004() {
        setPrincipal(1L, Role.ADMIN);
        TeacherCreateRequest request = validCreateRequest();
        teacherUser.setStatus(0);
        when(teacherMapper.countByTeacherNo("T001")).thenReturn(0);
        when(sysUserMapper.selectById(10L)).thenReturn(teacherUser);

        BusinessException ex = assertThrows(BusinessException.class, () -> teacherService.create(request));
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND.getCode(), ex.getCode());
        assertEquals("用户不存在", ex.getMessage());
    }

    @Test
    void create_duplicateUserId_throws40005() {
        setPrincipal(1L, Role.ADMIN);
        TeacherCreateRequest request = validCreateRequest();
        when(teacherMapper.countByTeacherNo("T001")).thenReturn(0);
        when(sysUserMapper.selectById(10L)).thenReturn(teacherUser);
        when(teacherMapper.countByUserId(10L)).thenReturn(1);

        BusinessException ex = assertThrows(BusinessException.class, () -> teacherService.create(request));
        assertEquals(ErrorCode.DATA_DUPLICATE.getCode(), ex.getCode());
        assertEquals("该用户已有教师档案", ex.getMessage());
    }

    @Test
    void create_success_returnsResponseWithEmptyCourseNames() {
        setPrincipal(1L, Role.ADMIN);
        TeacherCreateRequest request = validCreateRequest();
        when(teacherMapper.countByTeacherNo("T001")).thenReturn(0);
        when(sysUserMapper.selectById(10L)).thenReturn(teacherUser);
        when(teacherMapper.countByUserId(10L)).thenReturn(0);
        doAnswer(invocation -> {
            Teacher t = invocation.getArgument(0);
            t.setId(100L);
            t.setCreateTime(LocalDateTime.of(2024, 1, 1, 0, 0));
            return 1;
        }).when(teacherMapper).insert(any(Teacher.class));

        var response = teacherService.create(request);

        assertEquals(100L, response.getId());
        assertEquals(10L, response.getUserId());
        assertEquals("T001", response.getTeacherNo());
        assertEquals("张老师", response.getRealName());
        assertEquals("计算机学院", response.getDepartment());
        assertEquals(Collections.emptyList(), response.getCourseNames());
        verify(sysUserMapper, never()).deleteById(any(Long.class));
    }

    // --- getById ---

    @Test
    void getById_notFound_throws40004() {
        setPrincipal(1L, Role.ADMIN);
        when(teacherMapper.selectTeacherDetailById(99L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> teacherService.getById(99L));
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND.getCode(), ex.getCode());
        assertEquals("资源不存在", ex.getMessage());
    }

    @Test
    void getById_admin_canViewAny() {
        setPrincipal(1L, Role.ADMIN);
        TeacherDetailRow row = detailRow(100L, 10L);
        when(teacherMapper.selectTeacherDetailById(100L)).thenReturn(row);
        when(teacherMapper.selectCourseNamesByTeacherId(100L)).thenReturn(List.of("高等数学"));

        var response = teacherService.getById(100L);

        assertEquals(100L, response.getId());
        assertEquals(List.of("高等数学"), response.getCourseNames());
    }

    @Test
    void getById_teacherOwnProfile_returns200() {
        setPrincipal(10L, Role.TEACHER);
        TeacherDetailRow row = detailRow(100L, 10L);
        when(teacherMapper.selectTeacherDetailById(100L)).thenReturn(row);
        when(teacherMapper.selectCourseNamesByTeacherId(100L)).thenReturn(Collections.emptyList());

        var response = teacherService.getById(100L);

        assertEquals(100L, response.getId());
        assertEquals(Collections.emptyList(), response.getCourseNames());
    }

    @Test
    void getById_teacherOtherProfile_throws403() {
        setPrincipal(10L, Role.TEACHER);
        TeacherDetailRow row = detailRow(100L, 20L);
        when(teacherMapper.selectTeacherDetailById(100L)).thenReturn(row);

        assertThrows(AccessDeniedException.class, () -> teacherService.getById(100L));
    }

    // --- delete ---

    @Test
    void delete_hasCourseRelation_throws40006() {
        setPrincipal(1L, Role.ADMIN);
        Teacher teacher = teacherEntity(100L, 10L);
        when(teacherMapper.selectById(100L)).thenReturn(teacher);
        when(courseTeacherMapper.exists(any())).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> teacherService.delete(100L));
        assertEquals(ErrorCode.RELATION_EXISTS.getCode(), ex.getCode());
        assertEquals("该教师仍有关联课程，请先移除课程关联", ex.getMessage());
        verify(teacherMapper, never()).deleteById(any(Long.class));
        verify(sysUserMapper, never()).deleteById(any(Long.class));
    }

    @Test
    void delete_success_doesNotCascadeSysUser() {
        setPrincipal(1L, Role.ADMIN);
        Teacher teacher = teacherEntity(100L, 10L);
        when(teacherMapper.selectById(100L)).thenReturn(teacher);
        when(courseTeacherMapper.exists(any())).thenReturn(false);

        teacherService.delete(100L);

        verify(teacherMapper).deleteById(100L);
        verify(sysUserMapper, never()).deleteById(any(Long.class));
    }

    @Test
    void delete_notFound_throws40004() {
        setPrincipal(1L, Role.ADMIN);
        when(teacherMapper.selectById(99L)).thenReturn(null);

        assertThrows(BusinessException.class, () -> teacherService.delete(99L));
        verify(teacherMapper, never()).deleteById(any(Long.class));
    }

    // --- update ---

    @Test
    void update_nullDepartment_throws40000() {
        setPrincipal(1L, Role.ADMIN);
        TeacherUpdateRequest request = new TeacherUpdateRequest();

        BusinessException ex = assertThrows(BusinessException.class, () -> teacherService.update(100L, request));
        assertEquals(ErrorCode.PARAM_INVALID.getCode(), ex.getCode());
    }

    @Test
    void update_success_updatesDepartment() {
        setPrincipal(1L, Role.ADMIN);
        Teacher teacher = teacherEntity(100L, 10L);
        teacher.setDepartment("旧院系");
        TeacherUpdateRequest request = new TeacherUpdateRequest();
        request.setDepartment("新院系");

        when(teacherMapper.selectById(100L)).thenReturn(teacher);
        when(sysUserMapper.selectById(10L)).thenReturn(teacherUser);
        when(teacherMapper.selectCourseNamesByTeacherId(100L)).thenReturn(Collections.emptyList());

        var response = teacherService.update(100L, request);

        ArgumentCaptor<Teacher> captor = ArgumentCaptor.forClass(Teacher.class);
        verify(teacherMapper).updateById(captor.capture());
        assertEquals("新院系", captor.getValue().getDepartment());
        assertEquals("新院系", response.getDepartment());
    }

    private static TeacherCreateRequest validCreateRequest() {
        TeacherCreateRequest request = new TeacherCreateRequest();
        request.setUserId(10L);
        request.setTeacherNo("T001");
        request.setDepartment("计算机学院");
        return request;
    }

    private static TeacherDetailRow detailRow(Long id, Long userId) {
        TeacherDetailRow row = new TeacherDetailRow();
        row.setId(id);
        row.setUserId(userId);
        row.setTeacherNo("T001");
        row.setRealName("张老师");
        row.setDepartment("计算机学院");
        row.setCreateTime(LocalDateTime.of(2024, 1, 1, 0, 0));
        return row;
    }

    private static Teacher teacherEntity(Long id, Long userId) {
        Teacher teacher = new Teacher();
        teacher.setId(id);
        teacher.setUserId(userId);
        teacher.setTeacherNo("T001");
        teacher.setDepartment("计算机学院");
        return teacher;
    }

    private static void setPrincipal(Long userId, Role role) {
        SysUser user = new SysUser();
        user.setId(userId);
        user.setUsername("user" + userId);
        user.setPasswordHash("$2a$10$hash");
        user.setRole(role.name());
        user.setStatus(1);
        UserPrincipal principal = new UserPrincipal(user);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }
}
