package com.cmms11.inspection;

import com.cmms11.common.error.NotFoundException;
import com.cmms11.common.seq.AutoNumberService;
import com.cmms11.security.MemberUserDetailsService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 이름: InspectionService
 * 작성자: codex
 * 작성일: 2025-08-20
 * 수정일:
 * 프로그램 개요: 예방점검 트랜잭션 CRUD 로직을 담당하는 서비스.
 */
@Service
@Transactional
public class InspectionService {

    private static final String MODULE_CODE = "I";

    private final InspectionRepository repository;
    private final AutoNumberService autoNumberService;

    public InspectionService(InspectionRepository repository, AutoNumberService autoNumberService) {
        this.repository = repository;
        this.autoNumberService = autoNumberService;
    }

    @Transactional(readOnly = true)
    public Page<InspectionResponse> list(String keyword, Pageable pageable) {
        String companyId = MemberUserDetailsService.DEFAULT_COMPANY;
        Page<Inspection> page;
        if (keyword == null || keyword.isBlank()) {
            page = repository.findByIdCompanyId(companyId, pageable);
        } else {
            String trimmed = "%" + keyword.trim() + "%";
            page = repository.search(companyId, trimmed, pageable);
        }
        return page.map(InspectionResponse::from);
    }

    @Transactional(readOnly = true)
    public InspectionResponse get(String inspectionId) {
        return InspectionResponse.from(getExisting(inspectionId));
    }

    public InspectionResponse create(InspectionRequest request) {
        String companyId = MemberUserDetailsService.DEFAULT_COMPANY;
        LocalDateTime now = LocalDateTime.now();
        String memberId = currentMemberId();

        String newId = resolveId(companyId, request.inspectionId(), request.plannedDate());
        Inspection entity = new Inspection();
        entity.setId(new InspectionId(companyId, newId));
        entity.setCreatedAt(now);
        entity.setCreatedBy(memberId);
        applyRequest(entity, request);
        entity.setUpdatedAt(now);
        entity.setUpdatedBy(memberId);

        Inspection saved = repository.save(entity);
        return InspectionResponse.from(saved);
    }

    public InspectionResponse update(String inspectionId, InspectionRequest request) {
        Inspection entity = getExisting(inspectionId);
        applyRequest(entity, request);
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setUpdatedBy(currentMemberId());
        return InspectionResponse.from(repository.save(entity));
    }

    public void delete(String inspectionId) {
        Inspection entity = getExisting(inspectionId);
        repository.delete(entity);
    }

    private Inspection getExisting(String inspectionId) {
        return repository
            .findByIdCompanyIdAndIdInspectionId(MemberUserDetailsService.DEFAULT_COMPANY, inspectionId)
            .orElseThrow(() -> new NotFoundException("Inspection not found: " + inspectionId));
    }

    private void applyRequest(Inspection entity, InspectionRequest request) {
        entity.setName(request.name());
        entity.setPlantId(request.plantId());
        entity.setJobId(request.jobId());
        entity.setSiteId(request.siteId());
        entity.setDeptId(request.deptId());
        entity.setMemberId(request.memberId());
        entity.setPlannedDate(request.plannedDate());
        entity.setActualDate(request.actualDate());
        entity.setStatus(request.status());
        entity.setFileGroupId(request.fileGroupId());
        entity.setNote(request.note());
    }

    private String resolveId(String companyId, String requestedId, LocalDate referenceDate) {
        if (requestedId != null && !requestedId.isBlank()) {
            String trimmed = requestedId.trim();
            repository
                .findByIdCompanyIdAndIdInspectionId(companyId, trimmed)
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Inspection already exists: " + trimmed);
                });
            return trimmed;
        }
        LocalDate date = referenceDate != null ? referenceDate : LocalDate.now();
        return autoNumberService.generateTxId(companyId, MODULE_CODE, date);
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
