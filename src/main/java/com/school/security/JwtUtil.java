package com.school.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
public class JwtUtil {

    /** Token 类型 Claim 键名 */
    public static final String CLAIM_TOKEN_TYPE = "tokenType";
    /** Access Token 类型值 */
    public static final String TOKEN_TYPE_ACCESS  = "access";
    /** Refresh Token 类型值 */
    public static final String TOKEN_TYPE_REFRESH = "refresh";

    private final SecretKey key;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration}") long accessTokenExpiration,
            @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    public String generateAccessToken(Long userId, String username, String role) {
        return buildToken(userId, username, role, TOKEN_TYPE_ACCESS, accessTokenExpiration);
    }

    public String generateRefreshToken(Long userId, String username, String role) {
        return buildToken(userId, username, role, TOKEN_TYPE_REFRESH, refreshTokenExpiration);
    }

    private String buildToken(Long userId, String username, String role,
                               String tokenType, long expiration) {
        Date now = new Date();
        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .claim("role", role)
                .claim(CLAIM_TOKEN_TYPE, tokenType)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiration))
                .signWith(key)
                .compact();
    }

    /**
     * 解析 Token，返回 Claims；Token 无效或过期时记录 warn 日志并返回 null（M-1/M-4）。
     * 调用方无需重复解析，一次调用即可完成验证与数据提取。
     */
    public Claims parseTokenSafely(String token) {
        try {
            return parseToken(token);
        } catch (ExpiredJwtException e) {
            log.warn("JWT expired: sub={}", e.getClaims().getSubject());
        } catch (JwtException e) {
            log.warn("Invalid JWT signature or format: {}", e.getMessage());
        } catch (Exception e) {
            log.warn("JWT parse error: {}", e.getMessage());
        }
        return null;
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isValidToken(String token) {
        return parseTokenSafely(token) != null;
    }
}
