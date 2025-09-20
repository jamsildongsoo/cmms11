package com.cmms11.code;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 이름: CodeItemRepository
 * 작성자: codex
 * 작성일: 2025-08-20
 * 수정일:
 * 프로그램 개요: 공통 코드 항목(CodeItem) 접근 레포지토리.
 */
public interface CodeItemRepository extends JpaRepository<CodeItem, CodeItemId> {

    Page<CodeItem> findByIdCompanyIdAndIdCodeType(String companyId, String codeType, Pageable pageable);

    Page<CodeItem> findByIdCompanyIdAndIdCodeTypeAndNameContainingIgnoreCase(
        String companyId,
        String codeType,
        String name,
        Pageable pageable
    );

    List<CodeItem> findByIdCompanyIdAndIdCodeType(String companyId, String codeType);

    Optional<CodeItem> findByIdCompanyIdAndIdCodeTypeAndIdCode(String companyId, String codeType, String code);
}

