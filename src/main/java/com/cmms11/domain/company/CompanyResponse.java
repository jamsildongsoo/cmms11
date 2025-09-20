package com.cmms11.domain.company;

import java.time.LocalDateTime;

/**
 * 이름: CompanyResponse
 * 작성자: codex
 * 작성일: 2025-08-20
 * 수정일:
 * 프로그램 개요: 회사 조회 응답 DTO.
 */
public record CompanyResponse(
    String companyId,
    String name,
    String note,
    String deleteMark,
    LocalDateTime createdAt,
    String createdBy,
    LocalDateTime updatedAt,
    String updatedBy
) {

    public static CompanyResponse from(Company entity) {
        return new CompanyResponse(
            entity.getCompanyId(),
            entity.getName(),
            entity.getNote(),
            entity.getDeleteMark(),
            entity.getCreatedAt(),
            entity.getCreatedBy(),
            entity.getUpdatedAt(),
            entity.getUpdatedBy()
        );
    }
}

