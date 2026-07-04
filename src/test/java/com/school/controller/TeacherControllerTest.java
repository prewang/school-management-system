package com.school.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.common.enums.ErrorCode;
import com.school.common.result.PageResult;
import com.school.common.result.Result;
import com.school.dto.teacher.TeacherCreateRequest;
import com.school.dto.teacher.TeacherPageResponse;
import com.school.dto.teacher.TeacherUpdateRequest;
import com.school.service.TeacherService;
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

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Lightweight controller security tests: minimal Spring context with method security only,
 * does not load {@code SchoolAdminApplication} or JWT infrastructure.
 */
@SpringJUnitConfig(TeacherControllerTest.Config.class)
@WebAppConfiguration
class TeacherControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private TeacherService teacherService;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        reset(teacherService);
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void page_asTeacher_returns403() throws Exception {
        mockMvc.perform(get("/teachers").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void page_asStudent_returns403() throws Exception {
        mockMvc.perform(get("/teachers").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void page_asAdmin_returns200() throws Exception {
        PageResult<TeacherPageResponse> empty = new PageResult<>();
        empty.setRecords(Collections.emptyList());
        when(teacherService.page(any(), isNull(), isNull())).thenReturn(empty);

        mockMvc.perform(get("/teachers").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void create_asTeacher_returns403() throws Exception {
        TeacherCreateRequest request = new TeacherCreateRequest();
        request.setUserId(1L);
        request.setTeacherNo("T001");
        request.setDepartment("计算机学院");

        mockMvc.perform(post("/teachers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void update_asTeacher_returns403() throws Exception {
        TeacherUpdateRequest request = new TeacherUpdateRequest();
        request.setDepartment("新院系");

        mockMvc.perform(put("/teachers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void delete_asTeacher_returns403() throws Exception {
        mockMvc.perform(delete("/teachers/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Configuration
    @EnableWebMvc
    @EnableWebSecurity
    @EnableMethodSecurity
    @Import(TeacherController.class)
    static class Config {

        @Bean
        TeacherService teacherService() {
            return mock(TeacherService.class);
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
