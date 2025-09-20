package com.cmms11.common.seq;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SequenceRepository extends JpaRepository<Sequence, SequenceId> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Sequence s where s.id.companyId=:companyId and s.id.moduleCode=:moduleCode and s.id.dateKey=:dateKey")
    Optional<Sequence> findForUpdate(@Param("companyId") String companyId,
                                     @Param("moduleCode") String moduleCode,
                                     @Param("dateKey") String dateKey);
}

