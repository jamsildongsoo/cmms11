package com.cmms11.domain.func;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 이름: FuncId
 * 작성자: codex
 * 작성일: 2025-08-20
 * 수정일:
 * 프로그램 개요: 기능위치 식별자 복합키 정의.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class FuncId implements Serializable {

    @Column(name = "company_id", length = 5, nullable = false)
    private String companyId;

    @Column(name = "func_id", length = 5, nullable = false)
    private String funcId;
}

