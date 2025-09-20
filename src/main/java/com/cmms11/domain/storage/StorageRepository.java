package com.cmms11.domain.storage;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StorageRepository extends JpaRepository<Storage, StorageId> {
    Page<Storage> findByIdCompanyIdAndDeleteMark(String companyId, String deleteMark, Pageable pageable);

    @Query("select s from Storage s where s.id.companyId=:companyId and s.deleteMark=:deleteMark and (s.id.storageId like :q or s.name like :q)")
    Page<Storage> search(@Param("companyId") String companyId,
                         @Param("deleteMark") String deleteMark,
                         @Param("q") String q,
                         Pageable pageable);
}
