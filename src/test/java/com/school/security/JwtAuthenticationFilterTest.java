package com.school.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.common.enums.ErrorCode;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    private static final String TEST_SECRET =
            "7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c2d3e4f5a6b7c8d9e0f1a2b3c4d5e6f7a8b";

    private JwtUtil jwtUtil;
    private JwtAuthenticationFilter filter;

    @Mock
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        jwtUtil = new JwtUtil(TEST_SECRET, 7_200_000L, 604_800_000L);
        filter = new JwtAuthenticationFilter(jwtUtil, new ObjectMapper());
    }

    @Test
    void noToken_passesThroughWithoutAuthentication() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/users");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void validAccessToken_setsAuthentication() throws Exception {
        String token = jwtUtil.generateAccessToken(1L, "admin", "ADMIN");
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/users");
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertInstanceOf(UserPrincipal.class, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }

    @Test
    void invalidToken_returnsUnifiedUnauthorizedJson() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/users");
        request.addHeader("Authorization", "Bearer invalid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        assertEquals(401, response.getStatus());
        JsonNode body = new ObjectMapper().readTree(response.getContentAsString());
        assertEquals(ErrorCode.UNAUTHORIZED.getCode(), body.get("code").asInt());
        assertEquals(ErrorCode.UNAUTHORIZED.getMessage(), body.get("message").asText());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void refreshTokenAsBearer_returnsUnifiedUnauthorizedJson() throws Exception {
        String refreshToken = jwtUtil.generateRefreshToken(1L, "admin", "ADMIN");
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/users");
        request.addHeader("Authorization", "Bearer " + refreshToken);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        assertEquals(401, response.getStatus());
        JsonNode body = new ObjectMapper().readTree(response.getContentAsString());
        assertEquals(ErrorCode.UNAUTHORIZED.getCode(), body.get("code").asInt());
        assertEquals(ErrorCode.UNAUTHORIZED.getMessage(), body.get("message").asText());
    }
}
