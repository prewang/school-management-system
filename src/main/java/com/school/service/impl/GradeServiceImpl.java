package com.school.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.school.common.enums.ErrorCode;
import com.school.common.enums.Role;
import com.school.common.exception.BusinessException;
import com.school.common.request.PageRequest;
import com.school.common.result.PageResult;
import com.school.dto.grade.GradeCreateRequest;
import com.school.dto.grade.GradePageResponse;
import com.school.dto.grade.GradeResponse;
import com.school.dto.grade.GradeUpdateRequest;
import com.school.entity.Course;
import com.school.entity.Grade;
import com.school.entity.Student;
import com.school.entity.Teacher;
import com.school.mapper.CourseMapper;
import com.school.mapper.GradeMapper;
import com.school.mapper.StudentMapper;
import com.school.mapper.TeacherMapper;
import com.school.mapper.row.GradeListRow;
import com.school.security.UserPrincipal;
import com.school.service.GradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class GradeServiceImpl implements GradeService {

    private static final BigDecimal SCORE_MIN = BigDecimal.ZERO;
    private static final BigDecimal SCORE_MAX = new BigDecimal("100.00");

    private final GradeMapper gradeMapper;
    private final StudentMapper studentMapper;
    private final CourseMapper courseMapper;
    private final TeacherMapper teacherMapper;

    @Override
    @Transactional
    public GradeResponse create(GradeCreateRequest request) {
        Teacher teacher = resolveCurrentTeacher();
        if (teacher == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "资源不存在");
        }

        Student student = studentMapper.selectById(request.getStudentId());
        if (student == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "资源不存在");
        }

        Course course = courseMapper.selectById(request.getCourseId());
        if (course == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "资源不存在");
        }

        assertTeacherOwnsCourse(teacher.getId(), request.getCourseId());

        if (request.getScore().compareTo(SCORE_MIN) < 0 || request.getScore().compareTo(SCORE_MAX) > 0) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "分数须在 0~100 之间");
        }

        if (gradeMapper.countByStudentCourseSemester(
                request.getStudentId(), request.getCourseId(), request.getSemester()) > 0) {
            throw new BusinessException(ErrorCode.DATA_DUPLICATE,
                    "该学生本学期此课程成绩已存在，请使用修改接口");
        }

        Grade grade = new Grade();
        grade.setStudentId(request.getStudentId());
        grade.setCourseId(request.getCourseId());
        grade.setScore(request.getScore());
        grade.setSemester(request.getSemester());
        gradeMapper.insert(grade);

        return toGradeResponse(grade);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<GradePageResponse> pageMy(PageRequest pageRequest, String semester) {
        Student student = resolveCurrentStudent();
        if (student == null) {
            Page<GradePageResponse> empty = new Page<>(pageRequest.getPage(), pageRequest.getSize(), 0);
            empty.setRecords(Collections.emptyList());
            return PageResult.of(empty);
        }

        Page<GradeListRow> mpPage = new Page<>(pageRequest.getPage(), pageRequest.getSize());
        IPage<GradeListRow> result = gradeMapper.selectGradePage(
                mpPage, student.getId(), null, null, semester);
        IPage<GradePageResponse> dtoPage = result.convert(this::toGradePageResponse);
        return PageResult.of(dtoPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<GradePageResponse> page(PageRequest pageRequest, Long studentId, Long courseId,
                                            Long classId, String semester) {
        UserPrincipal principal = currentPrincipal();

        if (hasRole(principal, Role.STUDENT)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        Long queryStudentId;
        Long queryCourseId;
        Long queryClassId;

        if (hasRole(principal, Role.ADMIN) || hasRole(principal, Role.SUPER_ADMIN)) {
            queryStudentId = studentId;
            queryCourseId = courseId;
            queryClassId = classId;
        } else if (hasRole(principal, Role.TEACHER)) {
            if (courseId == null) {
                throw new BusinessException(ErrorCode.PARAM_INVALID, "courseId 不能为空");
            }
            if (studentId != null || classId != null) {
                throw new BusinessException(ErrorCode.PARAM_INVALID, "参数校验失败");
            }
            Teacher teacher = teacherMapper.selectOne(
                    new LambdaQueryWrapper<Teacher>().eq(Teacher::getUserId, principal.getId()));
            if (teacher == null) {
                throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "资源不存在");
            }
            assertTeacherOwnsCourse(teacher.getId(), courseId);
            queryStudentId = null;
            queryCourseId = courseId;
            queryClassId = null;
        } else {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        Page<GradeListRow> mpPage = new Page<>(pageRequest.getPage(), pageRequest.getSize());
        IPage<GradeListRow> result = gradeMapper.selectGradePage(
                mpPage, queryStudentId, queryCourseId, queryClassId, semester);
        IPage<GradePageResponse> dtoPage = result.convert(this::toGradePageResponse);
        return PageResult.of(dtoPage);
    }

    @Override
    @Transactional
    public GradeResponse update(Long id, GradeUpdateRequest request) {
        Teacher teacher = resolveCurrentTeacher();
        if (teacher == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "资源不存在");
        }

        Grade grade = gradeMapper.selectById(id);
        if (grade == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "资源不存在");
        }

        assertTeacherOwnsCourse(teacher.getId(), grade.getCourseId());

        if (request.getScore().compareTo(SCORE_MIN) < 0 || request.getScore().compareTo(SCORE_MAX) > 0) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "分数须在 0~100 之间");
        }

        grade.setScore(request.getScore());
        gradeMapper.updateById(grade);

        return toGradeResponse(grade);
    }

    private Student resolveCurrentStudent() {
        return studentMapper.selectOne(
                new LambdaQueryWrapper<Student>().eq(Student::getUserId, currentPrincipal().getId()));
    }

    private Teacher resolveCurrentTeacher() {
        return teacherMapper.selectOne(
                new LambdaQueryWrapper<Teacher>().eq(Teacher::getUserId, currentPrincipal().getId()));
    }

    private UserPrincipal currentPrincipal() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return principal;
    }

    private boolean hasRole(UserPrincipal principal, Role role) {
        return principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role.name()));
    }

    private void assertTeacherOwnsCourse(Long teacherId, Long courseId) {
        if (gradeMapper.existsByCourseIdAndTeacherId(courseId, teacherId) == 0) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权操作该课程成绩");
        }
    }

    private GradePageResponse toGradePageResponse(GradeListRow row) {
        GradePageResponse response = new GradePageResponse();
        response.setId(row.getId());
        response.setStudentId(row.getStudentId());
        response.setStudentRealName(row.getStudentRealName());
        response.setCourseId(row.getCourseId());
        response.setCourseName(row.getCourseName());
        response.setScore(row.getScore());
        response.setSemester(row.getSemester());
        return response;
    }

    private GradeResponse toGradeResponse(Grade grade) {
        GradeResponse response = new GradeResponse();
        response.setId(grade.getId());
        response.setStudentId(grade.getStudentId());
        response.setCourseId(grade.getCourseId());
        response.setScore(grade.getScore());
        response.setSemester(grade.getSemester());
        response.setCreateTime(grade.getCreateTime());
        return response;
    }
}
