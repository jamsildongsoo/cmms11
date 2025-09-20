package com.cmms11.code;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 이름: CodeItemRequest
 * 작성자: codex
 * 작성일: 2025-08-20
 * 수정일:
 * 프로그램 개요: 공통 코드 항목 생성/수정 요청 DTO.
 */
public record CodeItemRequest(
    @NotBlank @Size(max = 5) String codeType,
    @NotBlank @Size(max = 5) String code,
    @NotBlank @Size(max = 100) String name,
    @Size(max = 500) String note
) {
}

