package com.cmms11.memo;

import java.time.LocalDateTime;

/**
 * 이름: MemoResponse
 * 작성자: codex
 * 작성일: 2025-08-20
 * 수정일:
 * 프로그램 개요: 메모 응답 DTO.
 */
public record MemoResponse(
    String memoId,
    String title,
    String content,
    String refEntity,
    String refId,
    String fileGroupId,
    LocalDateTime createdAt,
    String createdBy,
    LocalDateTime updatedAt,
    String updatedBy
) {
    public static MemoResponse from(Memo memo) {
        String memoId = memo.getId() != null ? memo.getId().getMemoId() : null;
        return new MemoResponse(
            memoId,
            memo.getTitle(),
            memo.getContent(),
            memo.getRefEntity(),
            memo.getRefId(),
            memo.getFileGroupId(),
            memo.getCreatedAt(),
            memo.getCreatedBy(),
            memo.getUpdatedAt(),
            memo.getUpdatedBy()
        );
    }
}
