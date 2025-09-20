package com.cmms11.code;

import java.time.LocalDateTime;

/**
 * 이름: CodeTypeResponse
 * 작성자: codex
 * 작성일: 2025-08-20
 * 수정일:
 * 프로그램 개요: 공통 코드 타입 응답 DTO.
 */
public record CodeTypeResponse(
    String codeType,
    String name,
    String note,
    String deleteMark,
    LocalDateTime createdAt,
    String createdBy,
    LocalDateTime updatedAt,
    String updatedBy
) {

    public static CodeTypeResponse from(CodeType type) {
        return new CodeTypeResponse(
            type.getId().getCodeType(),
            type.getName(),
            type.getNote(),
            type.getDeleteMark(),
            type.getCreatedAt(),
            type.getCreatedBy(),
            type.getUpdatedAt(),
            type.getUpdatedBy()
        );
    }
}

