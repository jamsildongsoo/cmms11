package com.cmms11.inspection;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 이름: InspectionResponse
 * 작성자: codex
 * 작성일: 2025-08-20
 * 수정일:
 * 프로그램 개요: 예방점검 응답 DTO.
 */
public record InspectionResponse(
    String inspectionId,
    String name,
    String plantId,
    String jobId,
    String siteId,
    String deptId,
    String memberId,
    LocalDate plannedDate,
    LocalDate actualDate,
    String status,
    String fileGroupId,
    String note,
    LocalDateTime createdAt,
    String createdBy,
    LocalDateTime updatedAt,
    String updatedBy
) {
    public static InspectionResponse from(Inspection inspection) {
        String inspectionId = inspection.getId() != null ? inspection.getId().getInspectionId() : null;
        return new InspectionResponse(
            inspectionId,
            inspection.getName(),
            inspection.getPlantId(),
            inspection.getJobId(),
            inspection.getSiteId(),
            inspection.getDeptId(),
            inspection.getMemberId(),
            inspection.getPlannedDate(),
            inspection.getActualDate(),
            inspection.getStatus(),
            inspection.getFileGroupId(),
            inspection.getNote(),
            inspection.getCreatedAt(),
            inspection.getCreatedBy(),
            inspection.getUpdatedAt(),
            inspection.getUpdatedBy()
        );
    }
}
