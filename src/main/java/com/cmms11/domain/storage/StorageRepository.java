package com.cmms11.domain.storage;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 이름: StorageRepository
 * 작성자: codex
 * 작성일: 2025-08-20
 * 수정일:
 * 프로그램 개요: 창고(Storage) 엔티티 저장소.
 */
public interface StorageRepository extends JpaRepository<Storage, StorageId> {

    Page<Storage> findByIdCompanyIdAndDeleteMark(String companyId, String deleteMark, Pageable pageable);

    Page<Storage> findByIdCompanyIdAndDeleteMarkAndNameContainingIgnoreCase(
        String companyId,
        String deleteMark,
        String name,
        Pageable pageable
    );

    Optional<Storage> findByIdCompanyIdAndIdStorageId(String companyId, String storageId);
}

