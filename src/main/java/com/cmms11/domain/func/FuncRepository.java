package com.cmms11.domain.func;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 이름: FuncRepository
 * 작성자: codex
 * 작성일: 2025-08-20
 * 수정일:
 * 프로그램 개요: 기능위치 엔티티 영속성 접근 인터페이스.
 */
public interface FuncRepository extends JpaRepository<Func, FuncId> {

    Page<Func> findByIdCompanyIdAndDeleteMark(String companyId, String deleteMark, Pageable pageable);

    Page<Func> findByIdCompanyIdAndDeleteMarkAndNameContainingIgnoreCase(
        String companyId,
        String deleteMark,
        String name,
        Pageable pageable
    );

    Optional<Func> findByIdCompanyIdAndIdFuncId(String companyId, String funcId);
}


