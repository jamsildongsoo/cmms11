package com.cmms11.approval;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 이름: ApprovalRepository
 * 작성자: codex
 * 작성일: 2025-08-20
 * 수정일:
 * 프로그램 개요: 결재 헤더에 대한 CRUD 및 검색 레포지토리.
 */
public interface ApprovalRepository extends JpaRepository<Approval, ApprovalId> {

    Page<Approval> findByIdCompanyId(String companyId, Pageable pageable);

    @Query(
        "select a from Approval a " +
        "where a.id.companyId = :companyId and (a.id.approvalId like :keyword or a.title like :keyword)"
    )
    Page<Approval> search(
        @Param("companyId") String companyId,
        @Param("keyword") String keyword,
        Pageable pageable
    );

    Optional<Approval> findByIdCompanyIdAndIdApprovalId(String companyId, String approvalId);
}
