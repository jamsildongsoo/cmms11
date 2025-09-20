package com.cmms11.domain.site;

import java.time.LocalDateTime;

public record SiteResponse(
    String companyId,
    String siteId,
    String name,
    String note,
    String deleteMark,
    LocalDateTime createdAt,
    String createdBy,
    LocalDateTime updatedAt,
    String updatedBy
) {
    public static SiteResponse from(Site site) {
        return new SiteResponse(
            site.getId() != null ? site.getId().getCompanyId() : null,
            site.getId() != null ? site.getId().getSiteId() : null,
            site.getName(),
            site.getNote(),
            site.getDeleteMark(),
            site.getCreatedAt(),
            site.getCreatedBy(),
            site.getUpdatedAt(),
            site.getUpdatedBy()
        );
    }
}
