package com.cmms11.domain.dept;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 이름: DeptRepository
 * 작성자: codex
 * 작성일: 2025-08-20
 * 수정일:
 * 프로그램 개요: 부서(Dept) 엔티티 조회를 담당하는 JPA 레포지토리.
 */
public interface DeptRepository extends JpaRepository<Dept, DeptId> {

    Page<Dept> findByIdCompanyIdAndDeleteMark(String companyId, String deleteMark, Pageable pageable);

    Page<Dept> findByIdCompanyIdAndDeleteMarkAndNameContainingIgnoreCase(
        String companyId,
        String deleteMark,
        String name,
        Pageable pageable
    );

    Optional<Dept> findByIdCompanyIdAndIdDeptId(String companyId, String deptId);
}

