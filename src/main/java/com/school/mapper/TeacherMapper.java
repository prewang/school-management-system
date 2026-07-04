package com.school.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.school.entity.Teacher;
import com.school.mapper.row.TeacherDetailRow;
import com.school.mapper.row.TeacherListRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TeacherMapper extends BaseMapper<Teacher> {

    IPage<TeacherListRow> selectTeacherPage(Page<TeacherListRow> page,
                                            @Param("department") String department,
                                            @Param("keyword") String keyword);

    TeacherDetailRow selectTeacherDetailById(@Param("id") Long id);

    List<String> selectCourseNamesByTeacherId(@Param("teacherId") Long teacherId);

    /** Counts all rows including soft-deleted (DB unique key is not scoped by deleted). */
    int countByTeacherNo(@Param("teacherNo") String teacherNo);

    /** Counts all rows including soft-deleted (DB unique key is not scoped by deleted). */
    int countByUserId(@Param("userId") Long userId);
}
