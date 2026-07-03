package com.school.common.exception;

import com.school.common.enums.ErrorCode;
import com.school.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 将 BusinessException 的业务错误码映射到对应 HTTP 状态码：
     *   40101 (UNAUTHORIZED)    → 401
     *   40003 (ACCOUNT_DISABLED)→ 403
     *   40301 (FORBIDDEN)       → 403
     *   其余 4xxxx              → 400
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusinessException(BusinessException e) {
        HttpStatus status = resolveHttpStatus(e.getCode());
        return ResponseEntity.status(status).body(Result.fail(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return Result.fail(ErrorCode.PARAM_INVALID.getCode(), message);
    }

    /**
     * 安全网：Spring Security 的 AuthenticationException 正常由 ExceptionTranslationFilter
     * 在 Filter 层处理；此处作为防御性兜底，防止极端情况下它到达 MVC 层后被通用 500 处理器捕获。
     */
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Void> handleAuthenticationException(AuthenticationException e) {
        log.warn("AuthenticationException reached MVC layer: {}", e.getMessage());
        return Result.fail(ErrorCode.UNAUTHORIZED.getCode(), ErrorCode.UNAUTHORIZED.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleAccessDeniedException(AccessDeniedException e) {
        return Result.fail(ErrorCode.FORBIDDEN.getCode(), ErrorCode.FORBIDDEN.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e) {
        log.error("Unexpected error", e);
        return Result.fail(ErrorCode.INTERNAL_ERROR.getCode(), ErrorCode.INTERNAL_ERROR.getMessage());
    }

    private static HttpStatus resolveHttpStatus(int code) {
        if (code == ErrorCode.UNAUTHORIZED.getCode()) {
            return HttpStatus.UNAUTHORIZED;       // 40101 → 401
        }
        if (code == ErrorCode.ACCOUNT_DISABLED.getCode()
                || code == ErrorCode.FORBIDDEN.getCode()) {
            return HttpStatus.FORBIDDEN;          // 40003 / 40301 → 403
        }
        return HttpStatus.BAD_REQUEST;            // 其余业务错误 → 400
    }
}
