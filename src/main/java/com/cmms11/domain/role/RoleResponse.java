package com.cmms11.domain.role;

import java.time.LocalDateTime;

/**
 * 이름: RoleResponse
 * 작성자: codex
 * 작성일: 2025-08-20
 * 수정일:
 * 프로그램 개요: 권한 조회 응답 DTO.
 */
public record RoleResponse(
    String roleId,
    String name,
    String note,
    String deleteMark,
    LocalDateTime createdAt,
    String createdBy,
    LocalDateTime updatedAt,
    String updatedBy
) {

    public static RoleResponse from(Role role) {
        return new RoleResponse(
            role.getId().getRoleId(),
            role.getName(),
            role.getNote(),
            role.getDeleteMark(),
            role.getCreatedAt(),
            role.getCreatedBy(),
            role.getUpdatedAt(),
            role.getUpdatedBy()
        );
    }
}
