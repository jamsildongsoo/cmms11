package com.cmms11.domain.dept;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DeptRepository extends JpaRepository<Dept, DeptId> {

    Page<Dept> findByIdCompanyIdAndDeleteMark(String companyId, String deleteMark, Pageable pageable);

    @Query("select d from Dept d where d.id.companyId = :companyId and d.deleteMark = :deleteMark and (d.id.deptId like :q or d.name like :q)")
    Page<Dept> search(@Param("companyId") String companyId,
                      @Param("deleteMark") String deleteMark,
                      @Param("q") String q,
                      Pageable pageable);
}
