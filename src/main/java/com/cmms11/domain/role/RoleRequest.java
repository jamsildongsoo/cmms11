package com.cmms11.domain.role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 이름: RoleRequest
 * 작성자: codex
 * 작성일: 2025-08-20
 * 수정일:
 * 프로그램 개요: 권한 생성/수정 요청 DTO.
 */
public record RoleRequest(
    @NotBlank @Size(max = 5) String roleId,
    @NotBlank @Size(max = 100) String name,
    @Size(max = 500) String note
) {
}
