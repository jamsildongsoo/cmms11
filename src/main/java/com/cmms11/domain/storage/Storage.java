package com.cmms11.domain.storage;


import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 이름: Storage
 * 작성자: codex
 * 작성일: 2025-08-20
 * 수정일:
 * 프로그램 개요: 창고(Storage) 엔티티 정의.
 */

@Entity
@Table(name = "storage")
@Getter
@Setter
@NoArgsConstructor
public class Storage {

    @EmbeddedId
    private StorageId id;

    @Column(length = 100)
    private String name;

    @Column(length = 500)
    private String note;

    @Column(name = "delete_mark", length = 1)
    private String deleteMark = "N";

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 10)
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 10)
    private String updatedBy;
}
