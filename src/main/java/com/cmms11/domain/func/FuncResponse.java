package com.cmms11.domain.func;

import java.time.LocalDateTime;

/**
 * 이름: FuncResponse
 * 작성자: codex
 * 작성일: 2025-08-20
 * 수정일:
 * 프로그램 개요: 기능위치 조회 응답 DTO.
 */
public record FuncResponse(
    String funcId,
    String name,
    String note,
    String deleteMark,
    LocalDateTime createdAt,
    String createdBy,
    LocalDateTime updatedAt,
    String updatedBy
) {

    public static FuncResponse from(Func func) {
        return new FuncResponse(
            func.getId().getFuncId(),
            func.getName(),
            func.getNote(),
            func.getDeleteMark(),
            func.getCreatedAt(),
            func.getCreatedBy(),
            func.getUpdatedAt(),
            func.getUpdatedBy()
        );
    }
}

