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
    public static CompanyResponse from(Company company) {
        return new CompanyResponse(
            company.getCompanyId(),
            company.getName(),
            company.getNote(),
            company.getDeleteMark(),
            company.getCreatedAt(),
            company.getCreatedBy(),
            company.getUpdatedAt(),
            company.getUpdatedBy()
        );
    }
}
