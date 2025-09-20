package com.cmms11.code;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 이름: CodeTypeRepository
 * 작성자: codex
 * 작성일: 2025-08-20
 * 수정일:
 * 프로그램 개요: 공통 코드 타입 영속성 접근 레포지토리.
 */
public interface CodeTypeRepository extends JpaRepository<CodeType, CodeTypeId> {

    Page<CodeType> findByIdCompanyIdAndDeleteMark(String companyId, String deleteMark, Pageable pageable);

    Page<CodeType> findByIdCompanyIdAndDeleteMarkAndNameContainingIgnoreCase(
        String companyId,
        String deleteMark,
        String name,
        Pageable pageable
    );

    Optional<CodeType> findByIdCompanyIdAndIdCodeType(String companyId, String codeType);
}

