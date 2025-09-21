package com.cmms11.inspection;

public record InspectionItemResponse(
    Integer lineNo,
    String name,
    String method,
    String minVal,
    String maxVal,
    String stdVal,
    String unit,
    String resultVal,
    String note
) {
    public static InspectionItemResponse from(InspectionItem entity) {
        Integer lineNo = entity.getId() != null ? entity.getId().getLineNo() : null;
        return new InspectionItemResponse(
            lineNo,
            entity.getName(),
            entity.getMethod(),
            entity.getMinVal(),
            entity.getMaxVal(),
            entity.getStdVal(),
            entity.getUnit(),
            entity.getResultVal(),
            entity.getNote()
        );
    }
}
