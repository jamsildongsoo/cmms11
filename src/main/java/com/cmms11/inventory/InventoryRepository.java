package com.cmms11.inventory;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 이름: InventoryRepository
 * 작성자: codex
 * 작성일: 2025-08-20
 * 수정일:
 * 프로그램 개요: 재고 마스터 엔티티에 대한 기본 CRUD 및 검색 기능을 제공하는 JPA 레포지토리.
 */
public interface InventoryRepository extends JpaRepository<Inventory, InventoryId> {

    Page<Inventory> findByIdCompanyIdAndDeleteMark(String companyId, String deleteMark, Pageable pageable);

    @Query(
        "select i from Inventory i " +
        "where i.id.companyId = :companyId and i.deleteMark = :deleteMark " +
        "and (i.id.inventoryId like :keyword or i.name like :keyword)"
    )
    Page<Inventory> search(
        @Param("companyId") String companyId,
        @Param("deleteMark") String deleteMark,
        @Param("keyword") String keyword,
        Pageable pageable
    );

    Optional<Inventory> findByIdCompanyIdAndIdInventoryId(String companyId, String inventoryId);
}
