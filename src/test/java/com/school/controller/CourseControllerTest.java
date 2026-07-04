package com.school.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.common.enums.ErrorCode;
import com.school.common.result.PageResult;
import com.school.common.result.Result;
import com.school.dto.course.CoursePageResponse;
import com.school.service.CourseService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Lightweight controller security tests: minimal Spring context with method security only,
 * does not load {@code SchoolAdminApplication} or JWT infrastructure.
 */
@SpringJUnitConfig(CourseControllerTest.Config.class)
@WebAppConfiguration
class CourseControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private CourseService courseService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        reset(courseService);
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void page_asTeacher_returns403() throws Exception {
        mockMvc.perform(get("/courses").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void page_asStudent_returns403() throws Exception {
        mockMvc.perform(get("/courses").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void page_asAdmin_returns200() throws Exception {
        PageResult<CoursePageResponse> empty = new PageResult<>();
        empty.setRecords(Collections.emptyList());
        when(courseService.page(any(), isNull())).thenReturn(empty);

        mockMvc.perform(get("/courses").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void pageMy_asAdmin_returns403() throws Exception {
        mockMvc.perform(get("/courses/my").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void pageMy_asStudent_returns403() throws Exception {
        mockMvc.perform(get("/courses/my").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void pageMy_asTeacher_returns200() throws Exception {
        PageResult<CoursePageResponse> empty = new PageResult<>();
        empty.setRecords(Collections.emptyList());
        when(courseService.pageMy(any(), isNull())).thenReturn(empty);

        mockMvc.perform(get("/courses/my").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Configuration
    @EnableWebMvc
    @EnableWebSecurity
    @EnableMethodSecurity
    @Import(CourseController.class)
    static class Config {

        @Bean
        CourseService courseService() {
            return mock(CourseService.class);
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
