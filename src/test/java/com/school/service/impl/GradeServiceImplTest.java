package com.school.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.school.common.enums.ErrorCode;
import com.school.common.enums.Role;
import com.school.common.exception.BusinessException;
import com.school.common.request.PageRequest;
import com.school.dto.grade.GradeCreateRequest;
import com.school.dto.grade.GradeUpdateRequest;
import com.school.entity.Course;
import com.school.entity.Grade;
import com.school.entity.Student;
import com.school.entity.SysUser;
import com.school.entity.Teacher;
import com.school.mapper.CourseMapper;
import com.school.mapper.GradeMapper;
import com.school.mapper.StudentMapper;
import com.school.mapper.TeacherMapper;
import com.school.mapper.row.GradeListRow;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GradeServiceImplTest {

    @Mock
    private GradeMapper gradeMapper;

    @Mock
    private StudentMapper studentMapper;

    @Mock
    private CourseMapper courseMapper;

    @Mock
    private TeacherMapper teacherMapper;

    @InjectMocks
    private GradeServiceImpl gradeService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // --- create ---

    @Test
    void create_studentNotFound_throws40004() {
        setPrincipal(10L, Role.TEACHER);
        GradeCreateRequest request = validCreateRequest();
        when(teacherMapper.selectOne(any())).thenReturn(teacherEntity(5L, 10L));
        when(studentMapper.selectById(1L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> gradeService.create(request));
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND.getCode(), ex.getCode());
        assertEquals("资源不存在", ex.getMessage());
        verify(gradeMapper, never()).insert(any(Grade.class));
    }

    @Test
    void create_courseNotFound_throws40004() {
        setPrincipal(10L, Role.TEACHER);
        GradeCreateRequest request = validCreateRequest();
        when(teacherMapper.selectOne(any())).thenReturn(teacherEntity(5L, 10L));
        when(studentMapper.selectById(1L)).thenReturn(studentEntity(1L, 20L));
        when(courseMapper.selectById(2L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> gradeService.create(request));
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND.getCode(), ex.getCode());
        assertEquals("资源不存在", ex.getMessage());
        verify(gradeMapper, never()).insert(any(Grade.class));
    }

    @Test
    void create_otherTeacherCourse_throws40301() {
        setPrincipal(10L, Role.TEACHER);
        GradeCreateRequest request = validCreateRequest();
        when(teacherMapper.selectOne(any())).thenReturn(teacherEntity(5L, 10L));
        when(studentMapper.selectById(1L)).thenReturn(studentEntity(1L, 20L));
        when(courseMapper.selectById(2L)).thenReturn(courseEntity(2L));
        when(gradeMapper.existsByCourseIdAndTeacherId(2L, 5L)).thenReturn(0);

        BusinessException ex = assertThrows(BusinessException.class, () -> gradeService.create(request));
        assertEquals(ErrorCode.FORBIDDEN.getCode(), ex.getCode());
        assertEquals("无权操作该课程成绩", ex.getMessage());
        verify(gradeMapper, never()).insert(any(Grade.class));
    }

    @Test
    void create_scoreOutOfRange_throws400() {
        setPrincipal(10L, Role.TEACHER);
        GradeCreateRequest request = validCreateRequest();
        request.setScore(new BigDecimal("101.00"));
        when(teacherMapper.selectOne(any())).thenReturn(teacherEntity(5L, 10L));
        when(studentMapper.selectById(1L)).thenReturn(studentEntity(1L, 20L));
        when(courseMapper.selectById(2L)).thenReturn(courseEntity(2L));
        when(gradeMapper.existsByCourseIdAndTeacherId(2L, 5L)).thenReturn(1);

        BusinessException ex = assertThrows(BusinessException.class, () -> gradeService.create(request));
        assertEquals(ErrorCode.PARAM_INVALID.getCode(), ex.getCode());
        assertEquals("分数须在 0~100 之间", ex.getMessage());
        verify(gradeMapper, never()).insert(any(Grade.class));
    }

    @Test
    void create_duplicateGrade_throws400WithCustomMessage() {
        setPrincipal(10L, Role.TEACHER);
        GradeCreateRequest request = validCreateRequest();
        when(teacherMapper.selectOne(any())).thenReturn(teacherEntity(5L, 10L));
        when(studentMapper.selectById(1L)).thenReturn(studentEntity(1L, 20L));
        when(courseMapper.selectById(2L)).thenReturn(courseEntity(2L));
        when(gradeMapper.existsByCourseIdAndTeacherId(2L, 5L)).thenReturn(1);
        when(gradeMapper.countByStudentCourseSemester(1L, 2L, "2024-1")).thenReturn(1);

        BusinessException ex = assertThrows(BusinessException.class, () -> gradeService.create(request));
        assertEquals(ErrorCode.DATA_DUPLICATE.getCode(), ex.getCode());
        assertEquals("该学生本学期此课程成绩已存在，请使用修改接口", ex.getMessage());
        verify(gradeMapper, never()).insert(any(Grade.class));
    }

    @Test
    void create_success_insertsGrade() {
        setPrincipal(10L, Role.TEACHER);
        GradeCreateRequest request = validCreateRequest();
        when(teacherMapper.selectOne(any())).thenReturn(teacherEntity(5L, 10L));
        when(studentMapper.selectById(1L)).thenReturn(studentEntity(1L, 20L));
        when(courseMapper.selectById(2L)).thenReturn(courseEntity(2L));
        when(gradeMapper.existsByCourseIdAndTeacherId(2L, 5L)).thenReturn(1);
        when(gradeMapper.countByStudentCourseSemester(1L, 2L, "2024-1")).thenReturn(0);
        doAnswer(invocation -> {
            Grade grade = invocation.getArgument(0);
            grade.setId(100L);
            grade.setCreateTime(LocalDateTime.of(2024, 6, 1, 0, 0));
            return 1;
        }).when(gradeMapper).insert(any(Grade.class));

        var response = gradeService.create(request);

        assertEquals(100L, response.getId());
        assertEquals(1L, response.getStudentId());
        assertEquals(2L, response.getCourseId());
        assertEquals(new BigDecimal("85.50"), response.getScore());
        assertEquals("2024-1", response.getSemester());
    }

    // --- update ---

    @Test
    void update_otherTeacherCourse_throws40301() {
        setPrincipal(10L, Role.TEACHER);
        GradeUpdateRequest request = new GradeUpdateRequest();
        request.setScore(new BigDecimal("90.00"));
        when(teacherMapper.selectOne(any())).thenReturn(teacherEntity(5L, 10L));
        Grade grade = gradeEntity(100L, 1L, 2L);
        when(gradeMapper.selectById(100L)).thenReturn(grade);
        when(gradeMapper.existsByCourseIdAndTeacherId(2L, 5L)).thenReturn(0);

        BusinessException ex = assertThrows(BusinessException.class, () -> gradeService.update(100L, request));
        assertEquals(ErrorCode.FORBIDDEN.getCode(), ex.getCode());
        assertEquals("无权操作该课程成绩", ex.getMessage());
        verify(gradeMapper, never()).updateById(any(Grade.class));
    }

    @Test
    void update_scoreOutOfRange_throws400() {
        setPrincipal(10L, Role.TEACHER);
        GradeUpdateRequest request = new GradeUpdateRequest();
        request.setScore(new BigDecimal("-1.00"));
        when(teacherMapper.selectOne(any())).thenReturn(teacherEntity(5L, 10L));
        Grade grade = gradeEntity(100L, 1L, 2L);
        when(gradeMapper.selectById(100L)).thenReturn(grade);
        when(gradeMapper.existsByCourseIdAndTeacherId(2L, 5L)).thenReturn(1);

        BusinessException ex = assertThrows(BusinessException.class, () -> gradeService.update(100L, request));
        assertEquals(ErrorCode.PARAM_INVALID.getCode(), ex.getCode());
        assertEquals("分数须在 0~100 之间", ex.getMessage());
        verify(gradeMapper, never()).updateById(any(Grade.class));
    }

    // --- pageMy ---

    @Test
    void pageMy_filtersByCurrentStudentOnly() {
        setPrincipal(20L, Role.STUDENT);
        PageRequest pageRequest = new PageRequest();
        when(studentMapper.selectOne(any())).thenReturn(studentEntity(100L, 20L));

        GradeListRow row = gradeListRow(1L, 100L, "李四", 2L, "高等数学",
                new BigDecimal("88.00"), "2024-1");
        Page<GradeListRow> mpPage = pageWithRecords(row);
        when(gradeMapper.selectGradePage(any(Page.class), eq(100L), isNull(), isNull(), isNull()))
                .thenReturn(mpPage);

        var result = gradeService.pageMy(pageRequest, null);

        assertEquals(1, result.getTotal());
        assertEquals(100L, result.getRecords().get(0).getStudentId());
        assertEquals("高等数学", result.getRecords().get(0).getCourseName());
        verify(gradeMapper).selectGradePage(any(Page.class), eq(100L), isNull(), isNull(), isNull());
    }

    @Test
    void pageMy_noStudentProfile_returnsEmptyPage() {
        setPrincipal(20L, Role.STUDENT);
        PageRequest pageRequest = new PageRequest();
        when(studentMapper.selectOne(any())).thenReturn(null);

        var result = gradeService.pageMy(pageRequest, null);

        assertEquals(0, result.getTotal());
        assertEquals(Collections.emptyList(), result.getRecords());
        verify(gradeMapper, never()).selectGradePage(any(), any(), any(), any(), any());
    }

    // --- page ---

    @Test
    void page_teacherMissingCourseId_throws40000() {
        setPrincipal(10L, Role.TEACHER);
        PageRequest pageRequest = new PageRequest();

        BusinessException ex = assertThrows(BusinessException.class,
                () -> gradeService.page(pageRequest, null, null, null, null));
        assertEquals(ErrorCode.PARAM_INVALID.getCode(), ex.getCode());
        assertEquals("courseId 不能为空", ex.getMessage());
        verify(gradeMapper, never()).selectGradePage(any(), any(), any(), any(), any());
    }

    @Test
    void page_adminWithClassId_passesClassIdFilter() {
        setPrincipal(1L, Role.ADMIN);
        PageRequest pageRequest = new PageRequest();
        Page<GradeListRow> mpPage = pageWithRecords(
                gradeListRow(1L, 100L, "李四", 2L, "高等数学", new BigDecimal("90.00"), "2024-1"));
        when(gradeMapper.selectGradePage(any(Page.class), isNull(), isNull(), eq(5L), isNull()))
                .thenReturn(mpPage);

        var result = gradeService.page(pageRequest, null, null, 5L, null);

        assertEquals(1, result.getTotal());
        verify(gradeMapper).selectGradePage(any(Page.class), isNull(), isNull(), eq(5L), isNull());
    }

    @Test
    void page_teacherOwnCourse_queriesByCourseId() {
        setPrincipal(10L, Role.TEACHER);
        PageRequest pageRequest = new PageRequest();
        when(teacherMapper.selectOne(any())).thenReturn(teacherEntity(5L, 10L));
        when(gradeMapper.existsByCourseIdAndTeacherId(2L, 5L)).thenReturn(1);
        Page<GradeListRow> mpPage = pageWithRecords(
                gradeListRow(1L, 100L, "李四", 2L, "高等数学", new BigDecimal("90.00"), "2024-1"));
        when(gradeMapper.selectGradePage(any(Page.class), isNull(), eq(2L), isNull(), eq("2024-1")))
                .thenReturn(mpPage);

        var result = gradeService.page(pageRequest, null, 2L, null, "2024-1");

        assertEquals(1, result.getTotal());
        assertEquals("李四", result.getRecords().get(0).getStudentRealName());
        verify(gradeMapper).selectGradePage(any(Page.class), isNull(), eq(2L), isNull(), eq("2024-1"));
    }

    @Test
    void page_teacherOtherCourse_throws40301() {
        setPrincipal(10L, Role.TEACHER);
        PageRequest pageRequest = new PageRequest();
        when(teacherMapper.selectOne(any())).thenReturn(teacherEntity(5L, 10L));
        when(gradeMapper.existsByCourseIdAndTeacherId(99L, 5L)).thenReturn(0);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> gradeService.page(pageRequest, null, 99L, null, null));
        assertEquals(ErrorCode.FORBIDDEN.getCode(), ex.getCode());
        assertEquals("无权操作该课程成绩", ex.getMessage());
        verify(gradeMapper, never()).selectGradePage(any(), any(), any(), any(), any());
    }

    @Test
    void page_student_throws403() {
        setPrincipal(20L, Role.STUDENT);
        PageRequest pageRequest = new PageRequest();

        BusinessException ex = assertThrows(BusinessException.class,
                () -> gradeService.page(pageRequest, null, null, null, null));
        assertEquals(ErrorCode.FORBIDDEN.getCode(), ex.getCode());
        verify(gradeMapper, never()).selectGradePage(any(), any(), any(), any(), any());
    }

    private static GradeCreateRequest validCreateRequest() {
        GradeCreateRequest request = new GradeCreateRequest();
        request.setStudentId(1L);
        request.setCourseId(2L);
        request.setScore(new BigDecimal("85.50"));
        request.setSemester("2024-1");
        return request;
    }

    private static Teacher teacherEntity(Long id, Long userId) {
        Teacher teacher = new Teacher();
        teacher.setId(id);
        teacher.setUserId(userId);
        teacher.setTeacherNo("T001");
        teacher.setDepartment("计算机学院");
        return teacher;
    }

    private static Student studentEntity(Long id, Long userId) {
        Student student = new Student();
        student.setId(id);
        student.setUserId(userId);
        student.setClassId(5L);
        student.setStudentNo("S001");
        return student;
    }

    private static Course courseEntity(Long id) {
        Course course = new Course();
        course.setId(id);
        course.setName("高等数学");
        course.setCode("CS101");
        course.setCredit(3);
        return course;
    }

    private static Grade gradeEntity(Long id, Long studentId, Long courseId) {
        Grade grade = new Grade();
        grade.setId(id);
        grade.setStudentId(studentId);
        grade.setCourseId(courseId);
        grade.setScore(new BigDecimal("80.00"));
        grade.setSemester("2024-1");
        return grade;
    }

    private static GradeListRow gradeListRow(Long id, Long studentId, String studentRealName,
                                             Long courseId, String courseName,
                                             BigDecimal score, String semester) {
        GradeListRow row = new GradeListRow();
        row.setId(id);
        row.setStudentId(studentId);
        row.setStudentRealName(studentRealName);
        row.setCourseId(courseId);
        row.setCourseName(courseName);
        row.setScore(score);
        row.setSemester(semester);
        return row;
    }

    private static Page<GradeListRow> pageWithRecords(GradeListRow row) {
        Page<GradeListRow> page = new Page<>(1, 10);
        page.setRecords(List.of(row));
        page.setTotal(1);
        return page;
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
