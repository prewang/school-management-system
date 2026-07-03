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

        // Token 存在时，直接调用 parseToken() 并按异常类型返回定制化 401，
        // 使客户端能区分"过期（可刷新）"与"无效（需重新登录）"两种情况。
        Claims claims;
        try {
            claims = jwtUtil.parseToken(token);
        } catch (ExpiredJwtException e) {
            log.warn("JWT expired: sub={}", e.getClaims().getSubject());
            writeUnauthorized(response, "Token 已过期，请使用 Refresh Token 刷新或重新登录");
            return;
        } catch (JwtException e) {
            log.warn("Invalid JWT: {}", e.getMessage());
            writeUnauthorized(response, "Token 无效，请重新登录");
            return;
        } catch (Exception e) {
            log.warn("JWT parse error: {}", e.getMessage());
            writeUnauthorized(response, ErrorCode.UNAUTHORIZED.getMessage());
            return;
        }

        // H-2: 仅接受 Access Token，拒绝 Refresh Token 访问受保护接口
        if (!JwtUtil.TOKEN_TYPE_ACCESS.equals(claims.get(JwtUtil.CLAIM_TOKEN_TYPE))) {
            log.warn("Wrong token type used as Bearer: type={}", claims.get(JwtUtil.CLAIM_TOKEN_TYPE));
            writeUnauthorized(response, "Token 类型错误，请使用 Access Token");
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

    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
        response.getWriter().write(
                objectMapper.writeValueAsString(
                        Result.fail(ErrorCode.UNAUTHORIZED.getCode(), message)
                )
        );
    }
}
