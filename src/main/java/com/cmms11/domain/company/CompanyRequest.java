package com.cmms11.domain.company;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 이름: CompanyRequest
 * 작성자: codex
 * 작성일: 2025-08-20
 * 수정일:
 * 프로그램 개요: 회사 생성/수정 요청에 대한 DTO.
 */
public record CompanyRequest(
    @NotBlank @Size(max = 5) String companyId,
    @NotBlank @Size(max = 100) String name,
    @Size(max = 500) String note
) {

    public Company toEntity() {
        Company company = new Company();
        company.setCompanyId(companyId);
        company.setName(name);
        company.setNote(note);
        return company;
    }
}

