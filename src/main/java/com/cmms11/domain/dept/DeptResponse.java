package com.cmms11.domain.dept;

import java.time.LocalDateTime;

public record DeptResponse(
    String companyId,
    String deptId,
    String name,
    String note,
    String parentId,
    String deleteMark,
    LocalDateTime createdAt,
    String createdBy,
    LocalDateTime updatedAt,
    String updatedBy
) {
    public static DeptResponse from(Dept dept) {
        return new DeptResponse(
            dept.getId() != null ? dept.getId().getCompanyId() : null,
            dept.getId() != null ? dept.getId().getDeptId() : null,
            dept.getName(),
            dept.getNote(),
            dept.getParentId(),
            dept.getDeleteMark(),
            dept.getCreatedAt(),
            dept.getCreatedBy(),
            dept.getUpdatedAt(),
            dept.getUpdatedBy()
        );
    }
}
