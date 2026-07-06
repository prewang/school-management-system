package com.school.service.impl;

import com.school.common.enums.ErrorCode;
import com.school.common.exception.BusinessException;
import com.school.dto.schoolclass.SchoolClassCreateRequest;
import com.school.dto.schoolclass.SchoolClassUpdateRequest;
import com.school.entity.SchoolClass;
import com.school.mapper.SchoolClassMapper;
import com.school.mapper.StudentMapper;
import com.school.mapper.row.ClassStudentRow;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchoolClassServiceImplTest {

    @Mock
    private SchoolClassMapper schoolClassMapper;

    @Mock
    private StudentMapper studentMapper;

    @InjectMocks
    private SchoolClassServiceImpl schoolClassService;

    // --- create ---

    @Test
    void create_duplicateNameYear_throws40005() {
        SchoolClassCreateRequest request = validCreateRequest();
        when(schoolClassMapper.countByNameAndYear("高一(1)班", 2024, null)).thenReturn(1);

        BusinessException ex = assertThrows(BusinessException.class, () -> schoolClassService.create(request));
        assertEquals(ErrorCode.DATA_DUPLICATE.getCode(), ex.getCode());
        assertEquals("该年份下班级名称已存在", ex.getMessage());
        verify(schoolClassMapper, never()).insert(any(SchoolClass.class));
    }

    @Test
    void create_afterSoftDeletedSameName_succeeds() {
        SchoolClassCreateRequest request = validCreateRequest();
        when(schoolClassMapper.countByNameAndYear("高一(1)班", 2024, null)).thenReturn(0);

        doAnswer(invocation -> {
            SchoolClass c = invocation.getArgument(0);
            c.setId(5L);
            c.setCreateTime(LocalDateTime.of(2024, 9, 1, 0, 0));
            return 1;
        }).when(schoolClassMapper).insert(any(SchoolClass.class));

        var response = schoolClassService.create(request);

        assertEquals(5L, response.getId());
        verify(schoolClassMapper).insert(any(SchoolClass.class));
    }

    @Test
    void create_success_returnsResponseWithEmptyStudents() {
        SchoolClassCreateRequest request = validCreateRequest();
        when(schoolClassMapper.countByNameAndYear("高一(1)班", 2024, null)).thenReturn(0);
        doAnswer(invocation -> {
            SchoolClass c = invocation.getArgument(0);
            c.setId(1L);
            c.setCreateTime(LocalDateTime.of(2024, 9, 1, 0, 0));
            return 1;
        }).when(schoolClassMapper).insert(any(SchoolClass.class));

        var response = schoolClassService.create(request);

        assertEquals(1L, response.getId());
        assertEquals("高一(1)班", response.getName());
        assertEquals("高一", response.getGrade());
        assertEquals(2024, response.getYear());
        assertEquals(Collections.emptyList(), response.getStudents());
    }

    // --- getById ---

    @Test
    void getById_notFound_throws40004() {
        when(schoolClassMapper.selectById(99L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> schoolClassService.getById(99L));
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND.getCode(), ex.getCode());
        assertEquals("资源不存在", ex.getMessage());
    }

    @Test
    void getById_noStudents_returnsEmptyList() {
        SchoolClass schoolClass = schoolClassEntity(1L);
        when(schoolClassMapper.selectById(1L)).thenReturn(schoolClass);
        when(schoolClassMapper.selectStudentsByClassId(1L)).thenReturn(Collections.emptyList());

        var response = schoolClassService.getById(1L);

        assertEquals(1L, response.getId());
        assertEquals(Collections.emptyList(), response.getStudents());
    }

    @Test
    void getById_withStudents_returnsStudentList() {
        SchoolClass schoolClass = schoolClassEntity(1L);
        ClassStudentRow row = new ClassStudentRow();
        row.setId(10L);
        row.setStudentNo("S001");
        row.setRealName("张三");
        row.setGender(1);

        when(schoolClassMapper.selectById(1L)).thenReturn(schoolClass);
        when(schoolClassMapper.selectStudentsByClassId(1L)).thenReturn(List.of(row));

        var response = schoolClassService.getById(1L);

        assertEquals(1, response.getStudents().size());
        assertEquals("S001", response.getStudents().get(0).getStudentNo());
        assertEquals("张三", response.getStudents().get(0).getRealName());
    }

    // --- update ---

    @Test
    void update_allFieldsNull_throws40000() {
        SchoolClassUpdateRequest request = new SchoolClassUpdateRequest();

        BusinessException ex = assertThrows(BusinessException.class, () -> schoolClassService.update(1L, request));
        assertEquals(ErrorCode.PARAM_INVALID.getCode(), ex.getCode());
    }

    @Test
    void update_notFound_throws40004() {
        SchoolClassUpdateRequest request = new SchoolClassUpdateRequest();
        request.setName("高一(2)班");
        when(schoolClassMapper.selectById(99L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> schoolClassService.update(99L, request));
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND.getCode(), ex.getCode());
    }

    @Test
    void update_duplicateNameYear_throws40005() {
        SchoolClass schoolClass = schoolClassEntity(1L);
        SchoolClassUpdateRequest request = new SchoolClassUpdateRequest();
        request.setName("高一(2)班");

        when(schoolClassMapper.selectById(1L)).thenReturn(schoolClass);
        when(schoolClassMapper.countByNameAndYear("高一(2)班", 2024, 1L)).thenReturn(1);

        BusinessException ex = assertThrows(BusinessException.class, () -> schoolClassService.update(1L, request));
        assertEquals(ErrorCode.DATA_DUPLICATE.getCode(), ex.getCode());
        assertEquals("该年份下班级名称已存在", ex.getMessage());
        verify(schoolClassMapper, never()).updateById(any(SchoolClass.class));
    }

    @Test
    void update_success_returnsResponseWithEmptyStudents() {
        SchoolClass schoolClass = schoolClassEntity(1L);
        SchoolClassUpdateRequest request = new SchoolClassUpdateRequest();
        request.setName("高一(2)班");

        when(schoolClassMapper.selectById(1L)).thenReturn(schoolClass);
        when(schoolClassMapper.countByNameAndYear("高一(2)班", 2024, 1L)).thenReturn(0);

        var response = schoolClassService.update(1L, request);

        ArgumentCaptor<SchoolClass> captor = ArgumentCaptor.forClass(SchoolClass.class);
        verify(schoolClassMapper).updateById(captor.capture());
        assertEquals("高一(2)班", captor.getValue().getName());
        assertEquals("高一(2)班", response.getName());
        assertEquals(Collections.emptyList(), response.getStudents());
    }

    // --- delete ---

    @Test
    void delete_hasStudents_throws40006() {
        SchoolClass schoolClass = schoolClassEntity(1L);
        when(schoolClassMapper.selectById(1L)).thenReturn(schoolClass);
        when(studentMapper.exists(any())).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> schoolClassService.delete(1L));
        assertEquals(ErrorCode.RELATION_EXISTS.getCode(), ex.getCode());
        assertEquals("该班级下仍有学生，请先转移学生", ex.getMessage());
        verify(schoolClassMapper, never()).deleteById(any(Long.class));
    }

    @Test
    void delete_success_softDeletes() {
        SchoolClass schoolClass = schoolClassEntity(1L);
        when(schoolClassMapper.selectById(1L)).thenReturn(schoolClass);
        when(studentMapper.exists(any())).thenReturn(false);

        schoolClassService.delete(1L);

        verify(schoolClassMapper).deleteById(1L);
    }

    @Test
    void delete_notFound_throws40004() {
        when(schoolClassMapper.selectById(99L)).thenReturn(null);

        assertThrows(BusinessException.class, () -> schoolClassService.delete(99L));
        verify(schoolClassMapper, never()).deleteById(any(Long.class));
    }

    private static SchoolClassCreateRequest validCreateRequest() {
        SchoolClassCreateRequest request = new SchoolClassCreateRequest();
        request.setName("高一(1)班");
        request.setGrade("高一");
        request.setYear(2024);
        return request;
    }

    private static SchoolClass schoolClassEntity(Long id) {
        SchoolClass schoolClass = new SchoolClass();
        schoolClass.setId(id);
        schoolClass.setName("高一(1)班");
        schoolClass.setGrade("高一");
        schoolClass.setYear(2024);
        schoolClass.setCreateTime(LocalDateTime.of(2024, 9, 1, 0, 0));
        return schoolClass;
    }
}
