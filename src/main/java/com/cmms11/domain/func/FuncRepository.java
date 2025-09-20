package com.cmms11.domain.func;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FuncRepository extends JpaRepository<Func, FuncId> {
    Page<Func> findByIdCompanyIdAndDeleteMark(String companyId, String deleteMark, Pageable pageable);

    @Query("select f from Func f where f.id.companyId=:companyId and f.deleteMark=:deleteMark and (f.id.funcId like :q or f.name like :q)")
    Page<Func> search(@Param("companyId") String companyId,
                      @Param("deleteMark") String deleteMark,
                      @Param("q") String q,
                      Pageable pageable);
}
