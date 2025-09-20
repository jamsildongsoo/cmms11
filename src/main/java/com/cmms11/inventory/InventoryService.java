package com.cmms11.inventory;

import com.cmms11.common.error.NotFoundException;
import com.cmms11.common.seq.AutoNumberService;
import com.cmms11.security.MemberUserDetailsService;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 이름: InventoryService
 * 작성자: codex
 * 작성일: 2025-08-20
 * 수정일:
 * 프로그램 개요: 재고 마스터 CRUD 및 조회 로직을 담당하는 서비스.
 */
@Service
@Transactional
public class InventoryService {

    private static final String MODULE_CODE = "2"; // Inventory master per STRUCTURES.md

    private final InventoryRepository repository;
    private final AutoNumberService autoNumberService;

    public InventoryService(InventoryRepository repository, AutoNumberService autoNumberService) {
        this.repository = repository;
        this.autoNumberService = autoNumberService;
    }

    @Transactional(readOnly = true)
    public Page<InventoryResponse> list(String keyword, Pageable pageable) {
        String companyId = MemberUserDetailsService.DEFAULT_COMPANY;
        Page<Inventory> page;
        if (keyword == null || keyword.isBlank()) {
            page = repository.findByIdCompanyIdAndDeleteMark(companyId, "N", pageable);
        } else {
            String trimmed = "%" + keyword.trim() + "%";
            page = repository.search(companyId, "N", trimmed, pageable);
        }
        return page.map(InventoryResponse::from);
    }

    @Transactional(readOnly = true)
    public InventoryResponse get(String inventoryId) {
        return InventoryResponse.from(getActiveInventory(inventoryId));
    }

    public InventoryResponse create(InventoryRequest request) {
        String companyId = MemberUserDetailsService.DEFAULT_COMPANY;
        LocalDateTime now = LocalDateTime.now();
        String memberId = currentMemberId();
        String requestedId = request.inventoryId();

        Inventory entity;
        if (requestedId == null || requestedId.isBlank()) {
            String generatedId = autoNumberService.generateMasterId(companyId, MODULE_CODE);
            entity = new Inventory();
            entity.setId(new InventoryId(companyId, generatedId));
            entity.setCreatedAt(now);
            entity.setCreatedBy(memberId);
        } else {
            String trimmedId = requestedId.trim();
            InventoryId id = new InventoryId(companyId, trimmedId);
            Optional<Inventory> existing = repository.findById(id);
            if (existing.isPresent()) {
                entity = existing.get();
                if (!"Y".equalsIgnoreCase(entity.getDeleteMark())) {
                    throw new IllegalArgumentException("Inventory already exists: " + trimmedId);
                }
                if (entity.getCreatedAt() == null) {
                    entity.setCreatedAt(now);
                    entity.setCreatedBy(memberId);
                }
            } else {
                entity = new Inventory();
                entity.setId(id);
                entity.setCreatedAt(now);
                entity.setCreatedBy(memberId);
            }
        }

        applyRequest(entity, request);
        entity.setDeleteMark("N");
        entity.setUpdatedAt(now);
        entity.setUpdatedBy(memberId);

        return InventoryResponse.from(repository.save(entity));
    }

    public InventoryResponse update(String inventoryId, InventoryRequest request) {
        Inventory entity = getActiveInventory(inventoryId);
        applyRequest(entity, request);
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setUpdatedBy(currentMemberId());
        return InventoryResponse.from(repository.save(entity));
    }

    public void delete(String inventoryId) {
        Inventory entity = getActiveInventory(inventoryId);
        entity.setDeleteMark("Y");
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setUpdatedBy(currentMemberId());
        repository.save(entity);
    }

    private Inventory getActiveInventory(String inventoryId) {
        InventoryId id = new InventoryId(MemberUserDetailsService.DEFAULT_COMPANY, inventoryId);
        return repository.findById(id)
            .filter(inventory -> !"Y".equalsIgnoreCase(inventory.getDeleteMark()))
            .orElseThrow(() -> new NotFoundException("Inventory not found: " + inventoryId));
    }

    private void applyRequest(Inventory entity, InventoryRequest request) {
        entity.setName(request.name());
        entity.setAssetId(request.assetId());
        entity.setDeptId(request.deptId());
        entity.setMakerName(request.makerName());
        entity.setSpec(request.spec());
        entity.setModel(request.model());
        entity.setSerial(request.serial());
        entity.setFileGroupId(request.fileGroupId());
        entity.setNote(request.note());
        entity.setStatus(request.status());
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
