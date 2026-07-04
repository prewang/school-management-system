package com.school.service.impl;

import com.school.common.enums.ErrorCode;
import com.school.common.exception.BusinessException;
import com.school.dto.auth.LoginRequest;
import com.school.dto.auth.TokenResponse;
import com.school.entity.SysUser;
import com.school.mapper.SysUserMapper;
import com.school.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    private static final String TEST_SECRET =
            "7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c2d3e4f5a6b7c8d9e0f1a2b3c4d5e6f7a8b";

    @Mock
    private SysUserMapper sysUserMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    private JwtUtil jwtUtil;

    private AuthServiceImpl authService;

    private SysUser activeUser;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(TEST_SECRET, 7_200_000L, 604_800_000L);
        authService = new AuthServiceImpl(sysUserMapper, passwordEncoder, jwtUtil);

        activeUser = new SysUser();
        activeUser.setId(1L);
        activeUser.setUsername("admin");
        activeUser.setPasswordHash("$2a$10$hash");
        activeUser.setRole("ADMIN");
        activeUser.setStatus(1);
    }

    @Test
    void login_success_returnsTokens() {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("Admin@123456");

        when(sysUserMapper.selectOne(any())).thenReturn(activeUser);
        when(passwordEncoder.matches("Admin@123456", activeUser.getPasswordHash())).thenReturn(true);

        TokenResponse response = authService.login(request);

        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertEquals("ADMIN", response.getRole());
    }

    @Test
    void login_userNotFound_throwsUnauthorizedWithUnifiedMessage() {
        LoginRequest request = new LoginRequest();
        request.setUsername("nobody");
        request.setPassword("wrong");

        when(sysUserMapper.selectOne(any())).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.login(request));
        assertEquals(ErrorCode.UNAUTHORIZED.getCode(), ex.getCode());
        assertEquals("用户名或密码错误", ex.getMessage());
    }

    @Test
    void login_disabledAccount_throwsForbidden() {
        activeUser.setStatus(0);
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("Admin@123456");

        when(sysUserMapper.selectOne(any())).thenReturn(activeUser);

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.login(request));
        assertEquals(ErrorCode.ACCOUNT_DISABLED.getCode(), ex.getCode());
        assertEquals("账号已被禁用", ex.getMessage());
    }

    @Test
    void refresh_validToken_returnsNewAccessToken() {
        String refreshToken = jwtUtil.generateRefreshToken(
                activeUser.getId(), activeUser.getUsername(), activeUser.getRole());
        when(sysUserMapper.selectById(1L)).thenReturn(activeUser);

        TokenResponse response = authService.refresh(refreshToken);

        assertNotNull(response.getAccessToken());
        assertNull(response.getRefreshToken());
        assertEquals("ADMIN", response.getRole());
        assertTrue(jwtUtil.isValidToken(response.getAccessToken()));
    }

    @Test
    void refresh_userNotFound_throwsUnauthorized() {
        String refreshToken = jwtUtil.generateRefreshToken(99L, "ghost", "STUDENT");
        when(sysUserMapper.selectById(99L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.refresh(refreshToken));
        assertEquals(ErrorCode.UNAUTHORIZED.getCode(), ex.getCode());
        assertEquals("登录已过期，请重新登录", ex.getMessage());
    }

    @Test
    void refresh_disabledAccount_throwsForbidden() {
        activeUser.setStatus(0);
        String refreshToken = jwtUtil.generateRefreshToken(
                activeUser.getId(), activeUser.getUsername(), activeUser.getRole());
        when(sysUserMapper.selectById(1L)).thenReturn(activeUser);

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.refresh(refreshToken));
        assertEquals(ErrorCode.ACCOUNT_DISABLED.getCode(), ex.getCode());
        assertEquals("账号已被禁用", ex.getMessage());
    }

    @Test
    void refresh_accessTokenRejected_throwsUnauthorized() {
        String accessToken = jwtUtil.generateAccessToken(
                activeUser.getId(), activeUser.getUsername(), activeUser.getRole());

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.refresh(accessToken));
        assertEquals(ErrorCode.UNAUTHORIZED.getCode(), ex.getCode());
    }

    @Test
    void refresh_invalidToken_throwsUnauthorized() {
        BusinessException ex = assertThrows(BusinessException.class, () -> authService.refresh("bad-token"));
        assertEquals(ErrorCode.UNAUTHORIZED.getCode(), ex.getCode());
    }
}
