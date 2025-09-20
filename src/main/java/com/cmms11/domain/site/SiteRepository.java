package com.cmms11.domain.site;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 이름: SiteRepository
 * 작성자: codex
 * 작성일: 2025-08-20
 * 수정일:
 * 프로그램 개요: 사업장(Site) 엔티티 조회를 지원하는 JPA 레포지토리.
 */

public interface SiteRepository extends JpaRepository<Site, SiteId> {

    Page<Site> findByIdCompanyIdAndDeleteMark(String companyId, String deleteMark, Pageable pageable);

    Page<Site> findByIdCompanyIdAndDeleteMarkAndNameContainingIgnoreCase(
        String companyId,
        String deleteMark,
        String name,
        Pageable pageable
    );

    Optional<Site> findByIdCompanyIdAndIdSiteId(String companyId, String siteId);
}

