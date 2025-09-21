package com.cmms11.common.upload;

import java.util.List;

/**
 * 대량 업로드 처리 결과를 요약하는 응답 모델입니다.
 * 성공/실패 건수와 실패 행 정보를 포함합니다.
 */
public record BulkUploadResult(int successCount, int failureCount, List<BulkUploadError> errors) {
    public BulkUploadResult {
        errors = errors == null ? List.of() : List.copyOf(errors);
    }
}
