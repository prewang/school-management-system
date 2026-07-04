package com.school.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.school.entity.Grade;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface GradeMapper extends BaseMapper<Grade> {

    @Select("SELECT COUNT(1) FROM grade WHERE course_id = #{courseId} AND deleted = 0")
    int countByCourseId(@Param("courseId") Long courseId);
}
