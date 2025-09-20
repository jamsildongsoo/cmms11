package com.cmms11.domain.company;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 이름: CompanyRepository
 * 작성자: codex
 * 작성일: 2025-08-20
 * 수정일:
 * 프로그램 개요: 회사 엔티티에 대한 기본 CRUD 및 조회 메서드를 제공하는 JPA 레포지토리.
 */
public interface CompanyRepository extends JpaRepository<Company, String> {

    Page<Company> findByDeleteMark(String deleteMark, Pageable pageable);

    Page<Company> findByDeleteMarkAndNameContainingIgnoreCase(String deleteMark, String name, Pageable pageable);

    Optional<Company> findByCompanyIdAndDeleteMark(String companyId, String deleteMark);
}

