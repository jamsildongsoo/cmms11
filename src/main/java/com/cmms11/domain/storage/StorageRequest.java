package com.cmms11.domain.storage;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 이름: StorageRequest
 * 작성자: codex
 * 작성일: 2025-08-20
 * 수정일:
 * 프로그램 개요: 창고 생성/수정 요청 DTO.
 */
public record StorageRequest(
    @NotBlank @Size(max = 5) String storageId,
    @NotBlank @Size(max = 100) String name,
    @Size(max = 500) String note
) {
}

