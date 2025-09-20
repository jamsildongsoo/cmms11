package com.cmms11.domain.dept;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DeptCreateRequest(
    @NotBlank(message = "deptId is required")
    @Size(max = 5, message = "deptId must be at most 5 characters")
    String deptId,

    @NotBlank(message = "name is required")
    @Size(max = 100, message = "name must be at most 100 characters")
    String name,

    @Size(max = 500, message = "note must be at most 500 characters")
    String note,

    @Size(max = 5, message = "parentId must be at most 5 characters")
    String parentId
) {}
