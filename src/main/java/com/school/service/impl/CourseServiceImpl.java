package com.school.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.school.common.enums.ErrorCode;
import com.school.common.exception.BusinessException;
import com.school.common.request.PageRequest;
import com.school.common.result.PageResult;
import com.school.dto.course.CourseCreateRequest;
import com.school.dto.course.CoursePageResponse;
import com.school.dto.course.CourseResponse;
import com.school.dto.course.CourseTeacherAssignRequest;
import com.school.dto.course.CourseUpdateRequest;
import com.school.entity.Course;
import com.school.entity.CourseTeacher;
import com.school.entity.Teacher;
import com.school.mapper.CourseMapper;
import com.school.mapper.CourseTeacherMapper;
import com.school.mapper.GradeMapper;
import com.school.mapper.TeacherMapper;
import com.school.mapper.row.CourseListRow;
import com.school.mapper.row.CourseTeacherNameRow;
import com.school.security.UserPrincipal;
import com.school.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseMapper courseMapper;
    private final CourseTeacherMapper courseTeacherMapper;
    private final TeacherMapper teacherMapper;
    private final GradeMapper gradeMapper;

    @Override
    @Transactional
    public CourseResponse create(CourseCreateRequest request) {
        if (courseMapper.countByCode(request.getCode()) > 0) {
            throw new BusinessException(ErrorCode.DATA_DUPLICATE, "课程代码已存在");
        }

        Course course = new Course();
        course.setName(request.getName());
        course.setCode(request.getCode());
        course.setCredit(request.getCredit());
        courseMapper.insert(course);

        return toCourseResponse(course);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<CoursePageResponse> page(PageRequest pageRequest, String keyword) {
        Page<CourseListRow> mpPage = new Page<>(pageRequest.getPage(), pageRequest.getSize());
        IPage<CourseListRow> result = courseMapper.selectCoursePage(mpPage, keyword);

        List<Long> courseIds = result.getRecords().stream().map(CourseListRow::getId).toList();
        Map<Long, List<String>> teacherNamesByCourseId = loadTeacherNamesByCourseIds(courseIds);

        IPage<CoursePageResponse> dtoPage = result.convert(row -> toCoursePageResponse(row, teacherNamesByCourseId));
        return PageResult.of(dtoPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<CoursePageResponse> pageMy(PageRequest pageRequest, String keyword) {
        Teacher teacher = resolveCurrentTeacher();
        if (teacher == null) {
            Page<CoursePageResponse> empty = new Page<>(pageRequest.getPage(), pageRequest.getSize(), 0);
            empty.setRecords(Collections.emptyList());
            return PageResult.of(empty);
        }

        Page<CourseListRow> mpPage = new Page<>(pageRequest.getPage(), pageRequest.getSize());
        IPage<CourseListRow> result = courseMapper.selectMyCoursePage(mpPage, teacher.getId(), keyword);

        List<Long> courseIds = result.getRecords().stream().map(CourseListRow::getId).toList();
        Map<Long, List<String>> teacherNamesByCourseId = loadTeacherNamesByCourseIds(courseIds);

        IPage<CoursePageResponse> dtoPage = result.convert(row -> toCoursePageResponse(row, teacherNamesByCourseId));
        return PageResult.of(dtoPage);
    }

    @Override
    @Transactional
    public CourseResponse update(Long id, CourseUpdateRequest request) {
        if (request.getName() != null && request.getName().isBlank()) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "参数校验失败");
        }
        if (request.getName() == null && request.getCredit() == null) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "参数校验失败");
        }

        Course course = courseMapper.selectById(id);
        if (course == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "资源不存在");
        }

        if (request.getName() != null) {
            course.setName(request.getName());
        }
        if (request.getCredit() != null) {
            course.setCredit(request.getCredit());
        }

        courseMapper.updateById(course);
        return toCourseResponse(course);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Course course = courseMapper.selectById(id);
        if (course == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "资源不存在");
        }

        if (gradeMapper.countByCourseId(id) > 0) {
            throw new BusinessException(ErrorCode.RELATION_EXISTS, "该课程已有成绩数据，无法删除");
        }

        courseMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void assignTeachers(Long courseId, CourseTeacherAssignRequest request) {
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "资源不存在");
        }

        for (Long teacherId : request.getTeacherIds()) {
            Teacher teacher = teacherMapper.selectById(teacherId);
            if (teacher == null) {
                throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "资源不存在");
            }

            boolean exists = courseTeacherMapper.selectCount(
                    new LambdaQueryWrapper<CourseTeacher>()
                            .eq(CourseTeacher::getCourseId, courseId)
                            .eq(CourseTeacher::getTeacherId, teacherId)) > 0;
            if (exists) {
                continue;
            }

            CourseTeacher association = new CourseTeacher();
            association.setCourseId(courseId);
            association.setTeacherId(teacherId);
            courseTeacherMapper.insert(association);
        }
    }

    @Override
    @Transactional
    public void removeTeacher(Long courseId, Long teacherId) {
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "资源不存在");
        }

        CourseTeacher association = courseTeacherMapper.selectOne(
                new LambdaQueryWrapper<CourseTeacher>()
                        .eq(CourseTeacher::getCourseId, courseId)
                        .eq(CourseTeacher::getTeacherId, teacherId));
        if (association == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "资源不存在");
        }

        if (gradeMapper.countByCourseId(courseId) > 0) {
            throw new BusinessException(ErrorCode.RELATION_EXISTS, "该课程已有成绩数据，无法移除教师关联");
        }

        courseTeacherMapper.deleteById(association.getId());
    }

    private Teacher resolveCurrentTeacher() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return teacherMapper.selectOne(
                new LambdaQueryWrapper<Teacher>().eq(Teacher::getUserId, principal.getId()));
    }

    private Map<Long, List<String>> loadTeacherNamesByCourseIds(List<Long> courseIds) {
        if (courseIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<CourseTeacherNameRow> rows = courseMapper.selectTeacherNamesByCourseIds(courseIds);
        Map<Long, List<String>> map = new HashMap<>();
        for (CourseTeacherNameRow row : rows) {
            map.computeIfAbsent(row.getCourseId(), ignored -> new ArrayList<>())
                    .add(row.getTeacherName());
        }
        return map;
    }

    private CoursePageResponse toCoursePageResponse(CourseListRow row, Map<Long, List<String>> teacherNamesByCourseId) {
        CoursePageResponse response = new CoursePageResponse();
        response.setId(row.getId());
        response.setName(row.getName());
        response.setCode(row.getCode());
        response.setCredit(row.getCredit());
        response.setTeacherNames(teacherNamesByCourseId.getOrDefault(row.getId(), Collections.emptyList()));
        return response;
    }

    private CourseResponse toCourseResponse(Course course) {
        CourseResponse response = new CourseResponse();
        response.setId(course.getId());
        response.setName(course.getName());
        response.setCode(course.getCode());
        response.setCredit(course.getCredit());
        response.setCreateTime(course.getCreateTime());
        return response;
    }
}
