package com.cmms11.common.upload;

/**
 * CSV 업로드 처리 중 발생한 행 단위 오류 정보를 표현합니다.
 *
 * @param rowNumber CSV 상의 행 번호(헤더 포함, 1부터 시작)
 * @param message   사용자에게 전달할 오류 요약 메시지
 */
public record BulkUploadError(int rowNumber, String message) {
}
