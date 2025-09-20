package com.cmms11.domain.company;

import com.cmms11.common.error.NotFoundException;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 이름: CompanyService
 * 작성자: codex
 * 작성일: 2025-08-20
 * 수정일:
 * 프로그램 개요: 회사 기준정보 CRUD 및 조회 로직을 처리하는 서비스.
 */
@Service
@Transactional
public class CompanyService {

    private final CompanyRepository repository;

    public CompanyService(CompanyRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Page<CompanyResponse> list(String keyword, Pageable pageable) {
        Page<Company> page;
        if (keyword == null || keyword.isBlank()) {
            page = repository.findByDeleteMark("N", pageable);
        } else {
            page = repository.findByDeleteMarkAndNameContainingIgnoreCase("N", keyword.trim(), pageable);
        }
        return page.map(CompanyResponse::from);
    }

    @Transactional(readOnly = true)
    public CompanyResponse get(String companyId) {
        return CompanyResponse.from(getActiveCompany(companyId));
    }

    public CompanyResponse create(CompanyRequest request) {
        Optional<Company> existing = repository.findById(request.companyId());
        LocalDateTime now = LocalDateTime.now();
        String memberId = currentMemberId();

        if (existing.isPresent()) {
            Company entity = existing.get();
            if (!"Y".equalsIgnoreCase(entity.getDeleteMark())) {
                throw new IllegalArgumentException("Company already exists: " + request.companyId());
            }
            entity.setName(request.name());
            entity.setNote(request.note());
            entity.setDeleteMark("N");
            entity.setUpdatedAt(now);
            entity.setUpdatedBy(memberId);
            return CompanyResponse.from(repository.save(entity));
        }

        Company company = request.toEntity();
        company.setDeleteMark("N");
        company.setCreatedAt(now);
        company.setCreatedBy(memberId);
        company.setUpdatedAt(now);
        company.setUpdatedBy(memberId);
        return CompanyResponse.from(repository.save(company));
    }

    public CompanyResponse update(String companyId, CompanyRequest request) {
        Company existing = getActiveCompany(companyId);
        existing.setName(request.name());
        existing.setNote(request.note());
        existing.setUpdatedAt(LocalDateTime.now());
        existing.setUpdatedBy(currentMemberId());
        return CompanyResponse.from(repository.save(existing));
    }

    public void delete(String companyId) {
        Company existing = getActiveCompany(companyId);
        existing.setDeleteMark("Y");
        existing.setUpdatedAt(LocalDateTime.now());
        existing.setUpdatedBy(currentMemberId());
        repository.save(existing);
    }

    private Company getActiveCompany(String companyId) {
        return repository.findByCompanyIdAndDeleteMark(companyId, "N")
            .orElseThrow(() -> new NotFoundException("Company not found: " + companyId));
    }

    private String currentMemberId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "system";
        }
        String name = authentication.getName();
        return name != null ? name : "system";
    }
}

