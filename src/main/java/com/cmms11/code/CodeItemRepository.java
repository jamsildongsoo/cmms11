package com.cmms11.code;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CodeItemRepository extends JpaRepository<CodeItem, CodeItemId> {
    Page<CodeItem> findByIdCodeType(String codeType, Pageable pageable);
    Page<CodeItem> findByIdCodeTypeAndNameContainingIgnoreCase(String codeType, String q, Pageable pageable);
}

