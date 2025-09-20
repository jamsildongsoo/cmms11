package com.cmms11.code;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CodeTypeRepository extends JpaRepository<CodeType, CodeTypeId> {
    Page<CodeType> findByNameContainingIgnoreCase(String q, Pageable pageable);
}

