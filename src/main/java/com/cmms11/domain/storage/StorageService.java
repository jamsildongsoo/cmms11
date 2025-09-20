package com.cmms11.domain.storage;

import com.cmms11.common.error.NotFoundException;
import com.cmms11.security.MemberUserDetailsService;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class StorageService {
    private final StorageRepository repository;

    public StorageService(StorageRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Page<Storage> list(String q, Pageable pageable) {
        String companyId = MemberUserDetailsService.DEFAULT_COMPANY;
        if (q == null || q.isBlank()) {
            return repository.findByIdCompanyIdAndDeleteMark(companyId, "N", pageable);
        }
        return repository.search(companyId, "N", "%" + q + "%", pageable);
    }

    @Transactional(readOnly = true)
    public Storage get(String storageId) {
        StorageId id = new StorageId(MemberUserDetailsService.DEFAULT_COMPANY, storageId);
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Storage not found: " + storageId));
    }

    public Storage create(Storage storage) {
        if (storage.getId() == null || storage.getId().getStorageId() == null || storage.getId().getStorageId().isBlank()) {
            throw new IllegalArgumentException("storage.id.storageId is required");
        }
        String companyId = MemberUserDetailsService.DEFAULT_COMPANY;
        storage.getId().setCompanyId(companyId);
        if (storage.getDeleteMark() == null) {
            storage.setDeleteMark("N");
        }
        LocalDateTime now = LocalDateTime.now();
        if (storage.getCreatedAt() == null) {
            storage.setCreatedAt(now);
        }
        if (storage.getUpdatedAt() == null) {
            storage.setUpdatedAt(now);
        }
        return repository.save(storage);
    }

    public Storage update(String storageId, Storage storage) {
        String companyId = MemberUserDetailsService.DEFAULT_COMPANY;
        StorageId id = new StorageId(companyId, storageId);
        Storage existing = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Storage not found: " + storageId));
        storage.setId(id);
        if (storage.getDeleteMark() == null) {
            storage.setDeleteMark(existing.getDeleteMark());
        }
        if (storage.getCreatedAt() == null) {
            storage.setCreatedAt(existing.getCreatedAt());
        }
        if (storage.getCreatedBy() == null) {
            storage.setCreatedBy(existing.getCreatedBy());
        }
        if (storage.getUpdatedBy() == null) {
            storage.setUpdatedBy(existing.getUpdatedBy());
        }
        storage.setUpdatedAt(LocalDateTime.now());
        return repository.save(storage);
    }

    public void delete(String storageId) {
        String companyId = MemberUserDetailsService.DEFAULT_COMPANY;
        StorageId id = new StorageId(companyId, storageId);
        Storage existing = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Storage not found: " + storageId));
        existing.setDeleteMark("Y");
        existing.setUpdatedAt(LocalDateTime.now());
        repository.save(existing);
    }
}
