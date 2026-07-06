package com.school.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.school.entity.Grade;
import com.school.mapper.row.GradeListRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface GradeMapper extends BaseMapper<Grade> {

    @Select("SELECT COUNT(1) FROM grade WHERE course_id = #{courseId} AND deleted = 0")
    int countByCourseId(@Param("courseId") Long courseId);

    IPage<GradeListRow> selectGradePage(Page<GradeListRow> page,
                                        @Param("studentId") Long studentId,
                                        @Param("courseId") Long courseId,
                                        @Param("classId") Long classId,
                                        @Param("semester") String semester);

    int countByStudentCourseSemester(@Param("studentId") Long studentId,
                                     @Param("courseId") Long courseId,
                                     @Param("semester") String semester);

    int existsByCourseIdAndTeacherId(@Param("courseId") Long courseId,
                                     @Param("teacherId") Long teacherId);
}
