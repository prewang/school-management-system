package com.school.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.school.common.enums.ErrorCode;
import com.school.common.exception.BusinessException;
import com.school.dto.auth.LoginRequest;
import com.school.dto.auth.TokenResponse;
import com.school.entity.SysUser;
import com.school.mapper.SysUserMapper;
import com.school.security.JwtUtil;
import com.school.security.UserPrincipal;
import com.school.service.AuthService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public TokenResponse login(LoginRequest request) {
        // Step 1: 查询用户
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, request.getUsername())
        );
        if (user == null) {
            // 内部日志仅记录用户 ID（此处无 ID，记录脱敏后的 username 首字符）以便问题追踪，
            // 对外统一返回 40101 防枚举攻击
            log.warn("Login failed [USER_NOT_FOUND]: username={}", maskUsername(request.getUsername()));
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户名或密码错误");
        }

        // Step 2: 校验账号状态（先于 BCrypt，避免对禁用账号执行慢哈希运算）
        if (!Integer.valueOf(1).equals(user.getStatus())) {
            log.warn("Login failed [ACCOUNT_DISABLED]: userId={}", user.getId());
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED, "账号已被禁用");
        }

        // Step 3: BCrypt 校验密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            // 内部日志记录 userId，对外统一返回 40101 防枚举攻击
            log.warn("Login failed [PASSWORD_INCORRECT]: userId={}", user.getId());
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户名或密码错误");
        }

        // Step 4: 颁发 Token
        String accessToken  = jwtUtil.generateAccessToken(user.getId(), user.getUsername(), user.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername(), user.getRole());

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .role(user.getRole())
                .build();
    }

    @Override
    public void logout() {
        // 无状态 JWT 设计：服务端不维护 Token 状态，登出由客户端清除本地 Token 完成。
        // 此处仅记录 userId 供审计日志使用；生产环境可在此处将 jti 写入 Redis 黑名单。
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal principal) {
            log.info("User logged out: userId={}", principal.getId());
        }
    }

    /**
     * 对 username 做脱敏处理，保留首字符，其余替换为 *，避免明文用户名写入日志。
     * 例："admin" → "a****"
     */
    private static String maskUsername(String username) {
        if (username == null || username.isEmpty()) {
            return "****";
        }
        return username.charAt(0) + "*".repeat(Math.max(username.length() - 1, 4));
    }

    @Override
    public TokenResponse refresh(String refreshToken) {
        // Step 1: 解析 Token，同时完成签名验证与过期校验
        Claims claims = jwtUtil.parseTokenSafely(refreshToken);
        if (claims == null) {
            // Token 过期或非法，日志已由 parseTokenSafely 记录，不重复记录 Token 明文
            log.warn("Token refresh failed [INVALID_OR_EXPIRED_TOKEN]");
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "登录已过期，请重新登录");
        }

        // Step 2: 校验 tokenType 必须为 refresh，拒绝 Access Token 被误用于此接口
        String tokenType = claims.get(JwtUtil.CLAIM_TOKEN_TYPE, String.class);
        if (!JwtUtil.TOKEN_TYPE_REFRESH.equals(tokenType)) {
            log.warn("Token refresh failed [WRONG_TOKEN_TYPE]: tokenType={}", tokenType);
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "登录已过期，请重新登录");
        }

        // Step 3: 从 Claims 提取身份信息
        Long userId   = claims.get("userId", Long.class);
        String username = claims.getSubject();
        String role   = claims.get("role", String.class);

        // Step 4: 颁发新 Access Token（Spec 仅要求返回 Access Token，不做 Token Rotation）
        String newAccessToken = jwtUtil.generateAccessToken(userId, username, role);

        log.info("Token refreshed: userId={}", userId);

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .role(role)
                .build();
    }
}
