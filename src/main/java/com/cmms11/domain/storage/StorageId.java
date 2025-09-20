package com.cmms11.domain.storage;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 이름: StorageId
 * 작성자: codex
 * 작성일: 2025-08-20
 * 수정일:
 * 프로그램 개요: 창고(Storage) 복합키 정의.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class StorageId implements Serializable {

    @Column(name = "company_id", length = 5, nullable = false)
    private String companyId;

    @Column(name = "storage_id", length = 5, nullable = false)
    private String storageId;
}

