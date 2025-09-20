package com.cmms11.domain.func;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 이름: FuncRequest
 * 작성자: codex
 * 작성일: 2025-08-20
 * 수정일:
 * 프로그램 개요: 기능위치 생성/수정 요청 DTO.
 */
public record FuncRequest(
    @NotBlank @Size(max = 5) String funcId,
    @NotBlank @Size(max = 100) String name,
    @Size(max = 500) String note
) {
}

