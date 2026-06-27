package com.school.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.school.entity.Course;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CourseMapper extends BaseMapper<Course> {
}
