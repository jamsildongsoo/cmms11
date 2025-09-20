package com.cmms11.domain.company;

import java.time.LocalDateTime;

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

