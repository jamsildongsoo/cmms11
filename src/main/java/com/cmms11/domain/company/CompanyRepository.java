package com.cmms11.domain.company;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CompanyRepository extends JpaRepository<Company, String> {

    Page<Company> findByCompanyIdAndDeleteMark(String companyId, String deleteMark, Pageable pageable);

    @Query("select c from Company c where c.companyId = :companyId and c.deleteMark = :deleteMark and (c.companyId like :q or c.name like :q)")
    Page<Company> search(@Param("companyId") String companyId,
                         @Param("deleteMark") String deleteMark,
                         @Param("q") String q,
                         Pageable pageable);
}
