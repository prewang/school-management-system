package com.school.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.school.common.enums.ErrorCode;
import com.school.common.enums.Role;
import com.school.common.exception.BusinessException;
import com.school.common.request.PageRequest;
import com.school.dto.course.CourseCreateRequest;
import com.school.dto.course.CourseTeacherAssignRequest;
import com.school.dto.course.CourseUpdateRequest;
import com.school.entity.Course;
import com.school.entity.CourseTeacher;
import com.school.entity.SysUser;
import com.school.entity.Teacher;
import com.school.mapper.CourseMapper;
import com.school.mapper.CourseTeacherMapper;
import com.school.mapper.GradeMapper;
import com.school.mapper.TeacherMapper;
import com.school.mapper.row.CourseListRow;
import com.school.mapper.row.CourseTeacherNameRow;
import com.school.security.UserPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseServiceImplTest {

    @Mock
    private CourseMapper courseMapper;

    @Mock
    private CourseTeacherMapper courseTeacherMapper;

    @Mock
    private TeacherMapper teacherMapper;

    @Mock
    private GradeMapper gradeMapper;

    @InjectMocks
    private CourseServiceImpl courseService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // --- create ---

    @Test
    void create_duplicateCode_throws40005() {
        CourseCreateRequest request = validCreateRequest();
        when(courseMapper.countByCode("CS101")).thenReturn(1);

        BusinessException ex = assertThrows(BusinessException.class, () -> courseService.create(request));
        assertEquals(ErrorCode.DATA_DUPLICATE.getCode(), ex.getCode());
        assertEquals("课程代码已存在", ex.getMessage());
        verify(courseMapper, never()).insert(any(Course.class));
    }

    @Test
    void create_success_returnsResponse() {
        CourseCreateRequest request = validCreateRequest();
        when(courseMapper.countByCode("CS101")).thenReturn(0);
        doAnswer(invocation -> {
            Course course = invocation.getArgument(0);
            course.setId(1L);
            course.setCreateTime(LocalDateTime.of(2024, 1, 1, 0, 0));
            return 1;
        }).when(courseMapper).insert(any(Course.class));

        var response = courseService.create(request);

        assertEquals(1L, response.getId());
        assertEquals("高等数学", response.getName());
        assertEquals("CS101", response.getCode());
        assertEquals(3, response.getCredit());
    }

    // --- page ---

    @Test
    void page_returnsTeacherNames() {
        PageRequest pageRequest = new PageRequest();
        CourseListRow row = courseListRow(1L, "高等数学", "CS101", 3);
        Page<CourseListRow> mpPage = pageWithRecords(row);
        when(courseMapper.selectCoursePage(any(Page.class), eq("数学"))).thenReturn(mpPage);

        CourseTeacherNameRow nameRow = new CourseTeacherNameRow();
        nameRow.setCourseId(1L);
        nameRow.setTeacherName("张老师");
        when(courseMapper.selectTeacherNamesByCourseIds(List.of(1L))).thenReturn(List.of(nameRow));

        var result = courseService.page(pageRequest, "数学");

        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
        assertEquals(List.of("张老师"), result.getRecords().get(0).getTeacherNames());
    }

    @Test
    void page_noTeachers_returnsEmptyList() {
        PageRequest pageRequest = new PageRequest();
        CourseListRow row = courseListRow(1L, "高等数学", "CS101", 3);
        Page<CourseListRow> mpPage = pageWithRecords(row);
        when(courseMapper.selectCoursePage(any(Page.class), eq(null))).thenReturn(mpPage);
        when(courseMapper.selectTeacherNamesByCourseIds(List.of(1L))).thenReturn(Collections.emptyList());

        var result = courseService.page(pageRequest, null);

        assertEquals(Collections.emptyList(), result.getRecords().get(0).getTeacherNames());
    }

    // --- pageMy ---

    @Test
    void pageMy_noTeacherProfile_returnsEmptyPage() {
        setPrincipal(10L, Role.TEACHER);
        PageRequest pageRequest = new PageRequest();
        when(teacherMapper.selectOne(any())).thenReturn(null);

        var result = courseService.pageMy(pageRequest, null);

        assertEquals(0, result.getTotal());
        assertEquals(Collections.emptyList(), result.getRecords());
        verify(courseMapper, never()).selectMyCoursePage(any(), any(), any());
    }

    @Test
    void pageMy_teacherWithCourses_returnsPage() {
        setPrincipal(10L, Role.TEACHER);
        PageRequest pageRequest = new PageRequest();
        Teacher teacher = teacherEntity(5L, 10L);
        when(teacherMapper.selectOne(any())).thenReturn(teacher);

        CourseListRow row = courseListRow(1L, "高等数学", "CS101", 3);
        Page<CourseListRow> mpPage = pageWithRecords(row);
        when(courseMapper.selectMyCoursePage(any(Page.class), eq(5L), eq(null))).thenReturn(mpPage);
        when(courseMapper.selectTeacherNamesByCourseIds(List.of(1L))).thenReturn(Collections.emptyList());

        var result = courseService.pageMy(pageRequest, null);

        assertEquals(1, result.getTotal());
        assertEquals("高等数学", result.getRecords().get(0).getName());
    }

    @Test
    void pageMy_unauthenticated_throws401() {
        PageRequest pageRequest = new PageRequest();

        BusinessException ex = assertThrows(BusinessException.class, () -> courseService.pageMy(pageRequest, null));
        assertEquals(ErrorCode.UNAUTHORIZED.getCode(), ex.getCode());
    }

    // --- update ---

    @Test
    void update_noFields_throws40000() {
        CourseUpdateRequest request = new CourseUpdateRequest();

        BusinessException ex = assertThrows(BusinessException.class, () -> courseService.update(1L, request));
        assertEquals(ErrorCode.PARAM_INVALID.getCode(), ex.getCode());
    }

    @Test
    void update_blankName_throws40000() {
        CourseUpdateRequest request = new CourseUpdateRequest();
        request.setName("   ");

        BusinessException ex = assertThrows(BusinessException.class, () -> courseService.update(1L, request));
        assertEquals(ErrorCode.PARAM_INVALID.getCode(), ex.getCode());
        verify(courseMapper, never()).selectById(any(Long.class));
    }

    @Test
    void update_notFound_throws40004() {
        CourseUpdateRequest request = new CourseUpdateRequest();
        request.setCredit(4);
        when(courseMapper.selectById(99L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> courseService.update(99L, request));
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND.getCode(), ex.getCode());
        assertEquals("资源不存在", ex.getMessage());
    }

    @Test
    void update_success_updatesCredit() {
        Course course = courseEntity(1L);
        course.setCredit(3);
        CourseUpdateRequest request = new CourseUpdateRequest();
        request.setCredit(4);
        when(courseMapper.selectById(1L)).thenReturn(course);

        var response = courseService.update(1L, request);

        ArgumentCaptor<Course> captor = ArgumentCaptor.forClass(Course.class);
        verify(courseMapper).updateById(captor.capture());
        assertEquals(4, captor.getValue().getCredit());
        assertEquals(4, response.getCredit());
    }

    // --- delete ---

    @Test
    void delete_notFound_throws40004() {
        when(courseMapper.selectById(99L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> courseService.delete(99L));
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND.getCode(), ex.getCode());
        assertEquals("资源不存在", ex.getMessage());
        verify(courseMapper, never()).deleteById(any(Long.class));
    }

    @Test
    void delete_hasGrades_throws40006() {
        when(courseMapper.selectById(1L)).thenReturn(courseEntity(1L));
        when(gradeMapper.countByCourseId(1L)).thenReturn(1);

        BusinessException ex = assertThrows(BusinessException.class, () -> courseService.delete(1L));
        assertEquals(ErrorCode.RELATION_EXISTS.getCode(), ex.getCode());
        assertEquals("该课程已有成绩数据，无法删除", ex.getMessage());
        verify(courseMapper, never()).deleteById(any(Long.class));
    }

    @Test
    void delete_success_doesNotCascadeCourseTeacher() {
        when(courseMapper.selectById(1L)).thenReturn(courseEntity(1L));
        when(gradeMapper.countByCourseId(1L)).thenReturn(0);

        courseService.delete(1L);

        verify(courseMapper).deleteById(1L);
        verify(courseTeacherMapper, never()).delete(any());
    }

    // --- assignTeachers ---

    @Test
    void assignTeachers_courseNotFound_throws40004() {
        CourseTeacherAssignRequest request = assignRequest(2L);
        when(courseMapper.selectById(1L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> courseService.assignTeachers(1L, request));
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND.getCode(), ex.getCode());
        assertEquals("资源不存在", ex.getMessage());
    }

    @Test
    void assignTeachers_teacherNotFound_throws40004() {
        CourseTeacherAssignRequest request = assignRequest(99L);
        when(courseMapper.selectById(1L)).thenReturn(courseEntity(1L));
        when(teacherMapper.selectById(99L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> courseService.assignTeachers(1L, request));
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND.getCode(), ex.getCode());
        assertEquals("资源不存在", ex.getMessage());
    }

    @Test
    void assignTeachers_existingAssociation_isIdempotent() {
        CourseTeacherAssignRequest request = assignRequest(2L);
        when(courseMapper.selectById(1L)).thenReturn(courseEntity(1L));
        when(teacherMapper.selectById(2L)).thenReturn(teacherEntity(2L, 20L));
        when(courseTeacherMapper.selectCount(any())).thenReturn(1L);

        courseService.assignTeachers(1L, request);

        verify(courseTeacherMapper, never()).insert(any(CourseTeacher.class));
    }

    @Test
    void assignTeachers_newAssociation_inserts() {
        CourseTeacherAssignRequest request = assignRequest(2L);
        when(courseMapper.selectById(1L)).thenReturn(courseEntity(1L));
        when(teacherMapper.selectById(2L)).thenReturn(teacherEntity(2L, 20L));
        when(courseTeacherMapper.selectCount(any())).thenReturn(0L);

        courseService.assignTeachers(1L, request);

        ArgumentCaptor<CourseTeacher> captor = ArgumentCaptor.forClass(CourseTeacher.class);
        verify(courseTeacherMapper).insert(captor.capture());
        assertEquals(1L, captor.getValue().getCourseId());
        assertEquals(2L, captor.getValue().getTeacherId());
    }

    // --- removeTeacher ---

    @Test
    void removeTeacher_associationNotFound_throws40004() {
        when(courseMapper.selectById(1L)).thenReturn(courseEntity(1L));
        when(courseTeacherMapper.selectOne(any())).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> courseService.removeTeacher(1L, 2L));
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND.getCode(), ex.getCode());
        assertEquals("资源不存在", ex.getMessage());
    }

    @Test
    void removeTeacher_hasGrades_throws40006() {
        CourseTeacher association = new CourseTeacher();
        association.setId(10L);
        when(courseMapper.selectById(1L)).thenReturn(courseEntity(1L));
        when(courseTeacherMapper.selectOne(any())).thenReturn(association);
        when(gradeMapper.countByCourseId(1L)).thenReturn(1);

        BusinessException ex = assertThrows(BusinessException.class, () -> courseService.removeTeacher(1L, 2L));
        assertEquals(ErrorCode.RELATION_EXISTS.getCode(), ex.getCode());
        assertEquals("该课程已有成绩数据，无法移除教师关联", ex.getMessage());
        verify(courseTeacherMapper, never()).deleteById(any(Long.class));
    }

    @Test
    void removeTeacher_success_deletesAssociation() {
        CourseTeacher association = new CourseTeacher();
        association.setId(10L);
        when(courseMapper.selectById(1L)).thenReturn(courseEntity(1L));
        when(courseTeacherMapper.selectOne(any())).thenReturn(association);
        when(gradeMapper.countByCourseId(1L)).thenReturn(0);

        courseService.removeTeacher(1L, 2L);

        verify(courseTeacherMapper).deleteById(10L);
    }

    private static CourseCreateRequest validCreateRequest() {
        CourseCreateRequest request = new CourseCreateRequest();
        request.setName("高等数学");
        request.setCode("CS101");
        request.setCredit(3);
        return request;
    }

    private static CourseTeacherAssignRequest assignRequest(Long teacherId) {
        CourseTeacherAssignRequest request = new CourseTeacherAssignRequest();
        request.setTeacherIds(List.of(teacherId));
        return request;
    }

    private static Course courseEntity(Long id) {
        Course course = new Course();
        course.setId(id);
        course.setName("高等数学");
        course.setCode("CS101");
        course.setCredit(3);
        course.setCreateTime(LocalDateTime.of(2024, 1, 1, 0, 0));
        return course;
    }

    private static CourseListRow courseListRow(Long id, String name, String code, int credit) {
        CourseListRow row = new CourseListRow();
        row.setId(id);
        row.setName(name);
        row.setCode(code);
        row.setCredit(credit);
        return row;
    }

    private static Page<CourseListRow> pageWithRecords(CourseListRow row) {
        Page<CourseListRow> page = new Page<>(1, 10);
        page.setRecords(List.of(row));
        page.setTotal(1);
        return page;
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
