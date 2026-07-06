package com.school.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.school.entity.SchoolClass;
import com.school.mapper.row.ClassStudentRow;
import com.school.mapper.row.SchoolClassListRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SchoolClassMapper extends BaseMapper<SchoolClass> {

    IPage<SchoolClassListRow> selectSchoolClassPage(Page<SchoolClassListRow> page,
                                                    @Param("year") Integer year);

    List<ClassStudentRow> selectStudentsByClassId(@Param("classId") Long classId);

    /** Counts active rows only; soft-deleted classes do not block same-year name reuse. */
    int countByNameAndYear(@Param("name") String name,
                           @Param("year") Integer year,
                           @Param("excludeId") Long excludeId);
}
