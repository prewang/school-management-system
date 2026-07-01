package com.school.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.school.common.enums.ErrorCode;
import com.school.common.exception.BusinessException;
import com.school.dto.auth.LoginRequest;
import com.school.dto.auth.TokenResponse;
import com.school.entity.SysUser;
import com.school.mapper.SysUserMapper;
import com.school.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;

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

        // JWT 生成及 TokenResponse 构造将在 Task A-2 实现
        throw new UnsupportedOperationException("TODO: JWT generation pending Task A-2");
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
        throw new UnsupportedOperationException("TODO");
    }
}
