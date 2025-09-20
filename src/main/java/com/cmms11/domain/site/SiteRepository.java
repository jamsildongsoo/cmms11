package com.cmms11.domain.site;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SiteRepository extends JpaRepository<Site, SiteId> {

    Page<Site> findByIdCompanyIdAndDeleteMark(String companyId, String deleteMark, Pageable pageable);

    @Query("select s from Site s where s.id.companyId = :companyId and s.deleteMark = :deleteMark and (s.id.siteId like :q or s.name like :q)")
    Page<Site> search(@Param("companyId") String companyId,
                      @Param("deleteMark") String deleteMark,
                      @Param("q") String q,
                      Pageable pageable);
}
