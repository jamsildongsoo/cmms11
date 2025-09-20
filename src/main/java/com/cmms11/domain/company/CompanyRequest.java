package com.cmms11.domain.company;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CompanyRequest(
    @NotBlank(message = "name is required")
    @Size(max = 100, message = "name must be at most 100 characters")
    String name,

    @Size(max = 500, message = "note must be at most 500 characters")
    String note
) {}
