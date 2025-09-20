package com.cmms11.domain.site;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SiteCreateRequest(
    @NotBlank(message = "siteId is required")
    @Size(max = 5, message = "siteId must be at most 5 characters")
    String siteId,

    @NotBlank(message = "name is required")
    @Size(max = 100, message = "name must be at most 100 characters")
    String name,

    @Size(max = 500, message = "note must be at most 500 characters")
    String note
) {}
