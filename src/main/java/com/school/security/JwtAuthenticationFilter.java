package com.school.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.common.enums.ErrorCode;
import com.school.common.result.Result;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = extractToken(request);

        // 无 Token：放行，交由 authenticationEntryPoint 处理"未登录"场景
        if (!StringUtils.hasText(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Token 存在时解析并校验；对外统一返回 40101 标准消息，详细原因仅写日志。
        Claims claims;
        try {
            claims = jwtUtil.parseToken(token);
        } catch (ExpiredJwtException e) {
            log.warn("JWT expired: sub={}", e.getClaims().getSubject());
            writeUnauthorized(response);
            return;
        } catch (JwtException e) {
            log.warn("Invalid JWT: {}", e.getMessage());
            writeUnauthorized(response);
            return;
        } catch (Exception e) {
            log.warn("JWT parse error: {}", e.getMessage());
            writeUnauthorized(response);
            return;
        }

        // 仅接受 Access Token，拒绝 Refresh Token 访问受保护接口
        if (!JwtUtil.TOKEN_TYPE_ACCESS.equals(claims.get(JwtUtil.CLAIM_TOKEN_TYPE))) {
            log.warn("Wrong token type used as Bearer: type={}", claims.get(JwtUtil.CLAIM_TOKEN_TYPE));
            writeUnauthorized(response);
            return;
        }

        // M-2: 直接从 Claims 构建 UserPrincipal，无需查库
        UserPrincipal userPrincipal = new UserPrincipal(claims);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }

    private void writeUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
        response.getWriter().write(
                objectMapper.writeValueAsString(
                        Result.fail(ErrorCode.UNAUTHORIZED.getCode(), ErrorCode.UNAUTHORIZED.getMessage())
                )
        );
    }
}
