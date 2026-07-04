package com.school.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.school.entity.Course;
import com.school.mapper.row.CourseListRow;
import com.school.mapper.row.CourseTeacherNameRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CourseMapper extends BaseMapper<Course> {

    /** 统计课程代码数量（含已软删行，用于唯一性校验） */
    int countByCode(@Param("code") String code);

    IPage<CourseListRow> selectCoursePage(Page<CourseListRow> page, @Param("keyword") String keyword);

    IPage<CourseListRow> selectMyCoursePage(Page<CourseListRow> page,
                                            @Param("teacherId") Long teacherId,
                                            @Param("keyword") String keyword);

    List<CourseTeacherNameRow> selectTeacherNamesByCourseIds(@Param("courseIds") List<Long> courseIds);
}
