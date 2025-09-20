package com.cmms11.code;

/**
 * 이름: CodeItemResponse
 * 작성자: codex
 * 작성일: 2025-08-20
 * 수정일:
 * 프로그램 개요: 공통 코드 항목 응답 DTO.
 */
public record CodeItemResponse(
    String codeType,
    String code,
    String name,
    String note
) {

    public static CodeItemResponse from(CodeItem item) {
        return new CodeItemResponse(
            item.getId().getCodeType(),
            item.getId().getCode(),
            item.getName(),
            item.getNote()
        );
    }
}

