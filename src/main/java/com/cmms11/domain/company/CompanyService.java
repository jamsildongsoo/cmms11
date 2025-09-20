package com.cmms11.domain.company;

import com.cmms11.common.error.NotFoundException;
import com.cmms11.security.MemberUserDetailsService;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CompanyService {

    private static final String DELETE_MARK_ACTIVE = "N";
    private static final String DELETE_MARK_DELETED = "Y";
    private static final String DEFAULT_ACTOR = "system";

    private final CompanyRepository repository;

    public CompanyService(CompanyRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Page<Company> list(String q, Pageable pageable) {
        String companyId = MemberUserDetailsService.DEFAULT_COMPANY;
        if (q == null || q.isBlank()) {
            return repository.findByCompanyIdAndDeleteMark(companyId, DELETE_MARK_ACTIVE, pageable);
        }
        String like = "%" + q + "%";
        return repository.search(companyId, DELETE_MARK_ACTIVE, like, pageable);
    }

    @Transactional(readOnly = true)
    public Company get(String companyId) {
        validateCompanyId(companyId);
        return repository.findById(companyId)
            .filter(company -> !isDeleted(company.getDeleteMark()))
            .orElseThrow(() -> new NotFoundException("Company not found: " + companyId));
    }

    public Company create(CompanyRequest request, String actor) {
        String companyId = MemberUserDetailsService.DEFAULT_COMPANY;
        Company company = repository.findById(companyId).orElseGet(() -> {
            Company fresh = new Company();
            fresh.setCompanyId(companyId);
            return fresh;
        });

        if (!isDeleted(company.getDeleteMark()) && company.getCreatedAt() != null) {
            throw new IllegalStateException("Company already exists: " + companyId);
        }

        company.setName(request.name());
        company.setNote(request.note());
        company.setDeleteMark(DELETE_MARK_ACTIVE);
        LocalDateTime now = LocalDateTime.now();
        String resolvedActor = resolveActor(actor);
        company.setCreatedAt(now);
        company.setCreatedBy(resolvedActor);
        company.setUpdatedAt(now);
        company.setUpdatedBy(resolvedActor);
        return repository.save(company);
    }

    public Company update(String companyId, CompanyRequest request, String actor) {
        validateCompanyId(companyId);
        Company company = repository.findById(companyId)
            .filter(existing -> !isDeleted(existing.getDeleteMark()))
            .orElseThrow(() -> new NotFoundException("Company not found: " + companyId));

        company.setName(request.name());
        company.setNote(request.note());
        if (company.getDeleteMark() == null) {
            company.setDeleteMark(DELETE_MARK_ACTIVE);
        }
        company.setUpdatedAt(LocalDateTime.now());
        company.setUpdatedBy(resolveActor(actor));
        return repository.save(company);
    }

    public void delete(String companyId, String actor) {
        validateCompanyId(companyId);
        Company company = repository.findById(companyId)
            .filter(existing -> !isDeleted(existing.getDeleteMark()))
            .orElseThrow(() -> new NotFoundException("Company not found: " + companyId));

        company.setDeleteMark(DELETE_MARK_DELETED);
        company.setUpdatedAt(LocalDateTime.now());
        company.setUpdatedBy(resolveActor(actor));
        repository.save(company);
    }

    private void validateCompanyId(String companyId) {
        if (!MemberUserDetailsService.DEFAULT_COMPANY.equals(companyId)) {
            throw new NotFoundException("Company not found: " + companyId);
        }
    }

    private boolean isDeleted(String deleteMark) {
        return DELETE_MARK_DELETED.equalsIgnoreCase(deleteMark);
    }

    private String resolveActor(String actor) {
        return (actor == null || actor.isBlank()) ? DEFAULT_ACTOR : actor;
    }
}
