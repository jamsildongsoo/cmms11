package com.cmms11.workorder;

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
 * 이름: WorkOrderService
 * 작성자: codex
 * 작성일: 2025-08-20
 * 수정일:
 * 프로그램 개요: 작업지시 트랜잭션 CRUD 로직을 제공하는 서비스.
 */
@Service
@Transactional
public class WorkOrderService {

    private static final String MODULE_CODE = "O";

    private final WorkOrderRepository repository;
    private final AutoNumberService autoNumberService;

    public WorkOrderService(WorkOrderRepository repository, AutoNumberService autoNumberService) {
        this.repository = repository;
        this.autoNumberService = autoNumberService;
    }

    @Transactional(readOnly = true)
    public Page<WorkOrderResponse> list(String keyword, Pageable pageable) {
        String companyId = MemberUserDetailsService.DEFAULT_COMPANY;
        Page<WorkOrder> page;
        if (keyword == null || keyword.isBlank()) {
            page = repository.findByIdCompanyId(companyId, pageable);
        } else {
            String trimmed = "%" + keyword.trim() + "%";
            page = repository.search(companyId, trimmed, pageable);
        }
        return page.map(WorkOrderResponse::from);
    }

    @Transactional(readOnly = true)
    public WorkOrderResponse get(String workOrderId) {
        return WorkOrderResponse.from(getExisting(workOrderId));
    }

    public WorkOrderResponse create(WorkOrderRequest request) {
        String companyId = MemberUserDetailsService.DEFAULT_COMPANY;
        LocalDateTime now = LocalDateTime.now();
        String memberId = currentMemberId();

        String newId = resolveId(companyId, request.workOrderId(), request.plannedDate());
        WorkOrder entity = new WorkOrder();
        entity.setId(new WorkOrderId(companyId, newId));
        entity.setCreatedAt(now);
        entity.setCreatedBy(memberId);
        applyRequest(entity, request);
        entity.setUpdatedAt(now);
        entity.setUpdatedBy(memberId);

        return WorkOrderResponse.from(repository.save(entity));
    }

    public WorkOrderResponse update(String workOrderId, WorkOrderRequest request) {
        WorkOrder entity = getExisting(workOrderId);
        applyRequest(entity, request);
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setUpdatedBy(currentMemberId());
        return WorkOrderResponse.from(repository.save(entity));
    }

    public void delete(String workOrderId) {
        WorkOrder entity = getExisting(workOrderId);
        repository.delete(entity);
    }

    private WorkOrder getExisting(String workOrderId) {
        return repository
            .findByIdCompanyIdAndIdWorkOrderId(MemberUserDetailsService.DEFAULT_COMPANY, workOrderId)
            .orElseThrow(() -> new NotFoundException("WorkOrder not found: " + workOrderId));
    }

    private void applyRequest(WorkOrder entity, WorkOrderRequest request) {
        entity.setName(request.name());
        entity.setPlantId(request.plantId());
        entity.setJobId(request.jobId());
        entity.setSiteId(request.siteId());
        entity.setDeptId(request.deptId());
        entity.setMemberId(request.memberId());
        entity.setPlannedDate(request.plannedDate());
        entity.setPlannedCost(request.plannedCost());
        entity.setPlannedLabor(request.plannedLabor());
        entity.setActualDate(request.actualDate());
        entity.setActualCost(request.actualCost());
        entity.setActualLabor(request.actualLabor());
        entity.setStatus(request.status());
        entity.setFileGroupId(request.fileGroupId());
        entity.setNote(request.note());
    }

    private String resolveId(String companyId, String requestedId, LocalDate referenceDate) {
        if (requestedId != null && !requestedId.isBlank()) {
            String trimmed = requestedId.trim();
            repository
                .findByIdCompanyIdAndIdWorkOrderId(companyId, trimmed)
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("WorkOrder already exists: " + trimmed);
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
