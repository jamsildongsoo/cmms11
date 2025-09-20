package com.cmms11.common.error;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 이름: ApiErrorResponse
 * 작성자: codex
 * 작성일: 2025-08-20
 * 수정일:
 * 프로그램 개요: REST API 예외 응답 페이로드 정의.
 */
public record ApiErrorResponse(
    LocalDateTime timestamp,
    int status,
    String error,
    String message,
    String path,
    List<ApiFieldError> fieldErrors
) {

    public static ApiErrorResponse of(int status, String error, String message, String path) {
        return new ApiErrorResponse(LocalDateTime.now(), status, error, message, path, Collections.emptyList());
    }

    public static ApiErrorResponse of(
        int status,
        String error,
        String message,
        String path,
        List<ApiFieldError> fieldErrors
    ) {
        return new ApiErrorResponse(LocalDateTime.now(), status, error, message, path, fieldErrors);
    }

    /**
     * 이름: ApiFieldError
     * 작성자: codex
     * 작성일: 2025-08-20
     * 수정일:
     * 프로그램 개요: 필드 단위 Validation 오류 정보를 표현.
     */
    public record ApiFieldError(String field, String message, Object rejectedValue) {
    }
}

