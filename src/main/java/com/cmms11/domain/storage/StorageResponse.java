package com.cmms11.domain.storage;

import java.time.LocalDateTime;

/**
 * 이름: StorageResponse
 * 작성자: codex
 * 작성일: 2025-08-20
 * 수정일:
 * 프로그램 개요: 창고 조회 응답 DTO.
 */
public record StorageResponse(
    String storageId,
    String name,
    String note,
    String deleteMark,
    LocalDateTime createdAt,
    String createdBy,
    LocalDateTime updatedAt,
    String updatedBy
) {

    public static StorageResponse from(Storage storage) {
        return new StorageResponse(
            storage.getId().getStorageId(),
            storage.getName(),
            storage.getNote(),
            storage.getDeleteMark(),
            storage.getCreatedAt(),
            storage.getCreatedBy(),
            storage.getUpdatedAt(),
            storage.getUpdatedBy()
        );
    }
}

