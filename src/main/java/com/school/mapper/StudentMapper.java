package com.school.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.school.entity.Student;
import com.school.mapper.row.StudentDetailRow;
import com.school.mapper.row.StudentListRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface StudentMapper extends BaseMapper<Student> {

    IPage<StudentListRow> selectStudentPage(Page<StudentListRow> page,
                                            @Param("classId") Long classId,
                                            @Param("keyword") String keyword);

    StudentDetailRow selectStudentDetailById(@Param("id") Long id);

    /** Counts all rows including soft-deleted (DB unique key is not scoped by deleted). */
    int countByStudentNo(@Param("studentNo") String studentNo);

    /** Counts all rows including soft-deleted (DB unique key is not scoped by deleted). */
    int countByUserId(@Param("userId") Long userId);
}
