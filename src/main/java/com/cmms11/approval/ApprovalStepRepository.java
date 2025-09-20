package com.cmms11.approval;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 이름: ApprovalStepRepository
 * 작성자: codex
 * 작성일: 2025-08-20
 * 수정일:
 * 프로그램 개요: 결재 단계 엔티티용 JPA 레포지토리.
 */
public interface ApprovalStepRepository extends JpaRepository<ApprovalStep, ApprovalStepId> {

    List<ApprovalStep> findByIdCompanyIdAndIdApprovalIdOrderByIdStepNo(String companyId, String approvalId);

    void deleteByIdCompanyIdAndIdApprovalId(String companyId, String approvalId);
}
