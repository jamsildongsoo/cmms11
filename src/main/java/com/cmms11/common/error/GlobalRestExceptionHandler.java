package com.cmms11.common.error;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 이름: GlobalRestExceptionHandler
 * 작성자: codex
 * 작성일: 2025-08-20
 * 수정일:
 * 프로그램 개요: 공통 예외를 가로채어 표준화된 API 응답을 생성하는 핸들러.
 */
@Slf4j
@RestControllerAdvice
public class GlobalRestExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(NotFoundException ex, HttpServletRequest request) {
        log.warn("Resource not found: {}", ex.getMessage());
        HttpStatus status = HttpStatus.NOT_FOUND;
        ApiErrorResponse body = ApiErrorResponse.of(status.value(), status.getReasonPhrase(), ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiErrorResponse> handleUnauthorized(UnauthorizedException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        ApiErrorResponse body = ApiErrorResponse.of(status.value(), status.getReasonPhrase(), ex.getMessage(), request.getRequestURI());
        log.warn("Unauthorized access on {}: {}", request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        List<ApiErrorResponse.ApiFieldError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
            .map(this::toFieldError)
            .collect(Collectors.toList());
        ApiErrorResponse body = ApiErrorResponse.of(
            status.value(),
            status.getReasonPhrase(),
            "Validation failed",
            request.getRequestURI(),
            fieldErrors
        );
        log.warn("Validation error on {}: {}", request.getRequestURI(), fieldErrors);
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ApiErrorResponse> handleBadRequest(RuntimeException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ApiErrorResponse body = ApiErrorResponse.of(status.value(), status.getReasonPhrase(), ex.getMessage(), request.getRequestURI());
        log.warn("Business rule violation on {}: {}", request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.FORBIDDEN;
        ApiErrorResponse body = ApiErrorResponse.of(status.value(), status.getReasonPhrase(), ex.getMessage(), request.getRequestURI());
        log.warn("Access denied on {}: {}", request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoResourceFound(NoResourceFoundException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        String message = "요청하신 리소스를 찾을 수 없습니다: " + ex.getResourcePath();
        ApiErrorResponse body = ApiErrorResponse.of(status.value(), status.getReasonPhrase(), message, request.getRequestURI());
        log.warn("Resource not found: {} - {}", request.getRequestURI(), ex.getResourcePath());
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ApiErrorResponse body = ApiErrorResponse.of(status.value(), status.getReasonPhrase(), "Internal server error", request.getRequestURI());
        log.error("Unexpected error on {}", request.getRequestURI(), ex);
        return ResponseEntity.status(status).body(body);
    }

    private ApiErrorResponse.ApiFieldError toFieldError(FieldError fieldError) {
        return new ApiErrorResponse.ApiFieldError(fieldError.getField(), fieldError.getDefaultMessage(), fieldError.getRejectedValue());
    }
}

