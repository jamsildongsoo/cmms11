package com.cmms11.workorder;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 이름: WorkOrderRepository
 * 작성자: codex
 * 작성일: 2025-08-20
 * 수정일:
 * 프로그램 개요: 작업지시 엔티티에 대한 CRUD 및 검색 JPA 레포지토리.
 */
public interface WorkOrderRepository extends JpaRepository<WorkOrder, WorkOrderId> {

    Page<WorkOrder> findByIdCompanyId(String companyId, Pageable pageable);

    @Query(
        "select w from WorkOrder w " +
        "where w.id.companyId = :companyId and (w.id.orderId like :keyword or w.name like :keyword)"
    )
    Page<WorkOrder> search(
        @Param("companyId") String companyId,
        @Param("keyword") String keyword,
        Pageable pageable
    );

    Optional<WorkOrder> findByIdCompanyIdAndIdOrderId(String companyId, String orderId);
}
