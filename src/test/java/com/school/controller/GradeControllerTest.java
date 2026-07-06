package com.school.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.common.enums.ErrorCode;
import com.school.common.result.PageResult;
import com.school.common.result.Result;
import com.school.dto.grade.GradeCreateRequest;
import com.school.dto.grade.GradePageResponse;
import com.school.dto.grade.GradeUpdateRequest;
import com.school.service.GradeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.math.BigDecimal;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringJUnitConfig(GradeControllerTest.Config.class)
@WebAppConfiguration
class GradeControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private GradeService gradeService;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        reset(gradeService);
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void page_asStudent_returns403() throws Exception {
        mockMvc.perform(get("/grades").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.FORBIDDEN.getCode()));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void pageMy_asStudent_returns200() throws Exception {
        PageResult<GradePageResponse> empty = new PageResult<>();
        empty.setRecords(Collections.emptyList());
        empty.setTotal(0);
        empty.setPages(0);
        empty.setCurrent(1);
        empty.setSize(10);
        when(gradeService.pageMy(any(), isNull())).thenReturn(empty);

        mockMvc.perform(get("/grades/my").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_asAdmin_returns403() throws Exception {
        GradeCreateRequest request = new GradeCreateRequest();
        request.setStudentId(1L);
        request.setCourseId(2L);
        request.setScore(new BigDecimal("80.00"));
        request.setSemester("2024-1");

        mockMvc.perform(post("/grades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.FORBIDDEN.getCode()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void update_asAdmin_returns403() throws Exception {
        GradeUpdateRequest request = new GradeUpdateRequest();
        request.setScore(new BigDecimal("90.00"));

        mockMvc.perform(put("/grades/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.FORBIDDEN.getCode()));
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void page_asTeacher_returns200() throws Exception {
        PageResult<GradePageResponse> empty = new PageResult<>();
        empty.setRecords(Collections.emptyList());
        when(gradeService.page(any(), isNull(), eq(2L), isNull(), isNull())).thenReturn(empty);

        mockMvc.perform(get("/grades").param("courseId", "2").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Configuration
    @EnableWebMvc
    @EnableWebSecurity
    @EnableMethodSecurity
    @Import(GradeController.class)
    static class Config {

        @Bean
        GradeService gradeService() {
            return mock(GradeService.class);
        }

        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }

        @Bean
        SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            return http
                    .csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                    .build();
        }

        @Bean
        MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
            return new MappingJackson2HttpMessageConverter();
        }

        @Bean
        LocalValidatorFactoryBean validator() {
            return new LocalValidatorFactoryBean();
        }

        @Bean
        AuthorizationDeniedAdvice authorizationDeniedAdvice() {
            return new AuthorizationDeniedAdvice();
        }
    }

    @RestControllerAdvice
    static class AuthorizationDeniedAdvice {
        @ExceptionHandler(AuthorizationDeniedException.class)
        @ResponseStatus(HttpStatus.FORBIDDEN)
        public Result<Void> handleAuthorizationDenied() {
            return Result.fail(ErrorCode.FORBIDDEN.getCode(), ErrorCode.FORBIDDEN.getMessage());
        }
    }
}
