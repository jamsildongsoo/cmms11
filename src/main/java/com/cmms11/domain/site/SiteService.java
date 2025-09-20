package com.cmms11.domain.site;

import com.cmms11.common.error.NotFoundException;
import com.cmms11.security.MemberUserDetailsService;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SiteService {

    private static final String DELETE_MARK_ACTIVE = "N";
    private static final String DELETE_MARK_DELETED = "Y";
    private static final String DEFAULT_ACTOR = "system";

    private final SiteRepository repository;

    public SiteService(SiteRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Page<Site> list(String q, Pageable pageable) {
        String companyId = MemberUserDetailsService.DEFAULT_COMPANY;
        if (q == null || q.isBlank()) {
            return repository.findByIdCompanyIdAndDeleteMark(companyId, DELETE_MARK_ACTIVE, pageable);
        }
        String like = "%" + q + "%";
        return repository.search(companyId, DELETE_MARK_ACTIVE, like, pageable);
    }

    @Transactional(readOnly = true)
    public Site get(String siteId) {
        Site site = repository.findById(buildId(siteId))
            .filter(existing -> !isDeleted(existing.getDeleteMark()))
            .orElseThrow(() -> new NotFoundException("Site not found: " + siteId));
        if (site.getDeleteMark() == null) {
            site.setDeleteMark(DELETE_MARK_ACTIVE);
        }
        return site;
    }

    public Site create(SiteCreateRequest request, String actor) {
        SiteId id = buildId(request.siteId());
        Site site = repository.findById(id).orElseGet(() -> {
            Site fresh = new Site();
            fresh.setId(id);
            return fresh;
        });

        if (!isDeleted(site.getDeleteMark()) && site.getCreatedAt() != null) {
            throw new IllegalStateException("Site already exists: " + request.siteId());
        }

        site.setName(request.name());
        site.setNote(request.note());
        site.setDeleteMark(DELETE_MARK_ACTIVE);
        LocalDateTime now = LocalDateTime.now();
        String resolvedActor = resolveActor(actor);
        site.setCreatedAt(now);
        site.setCreatedBy(resolvedActor);
        site.setUpdatedAt(now);
        site.setUpdatedBy(resolvedActor);
        return repository.save(site);
    }

    public Site update(String siteId, SiteUpdateRequest request, String actor) {
        Site site = repository.findById(buildId(siteId))
            .filter(existing -> !isDeleted(existing.getDeleteMark()))
            .orElseThrow(() -> new NotFoundException("Site not found: " + siteId));

        site.setName(request.name());
        site.setNote(request.note());
        if (site.getDeleteMark() == null) {
            site.setDeleteMark(DELETE_MARK_ACTIVE);
        }
        site.setUpdatedAt(LocalDateTime.now());
        site.setUpdatedBy(resolveActor(actor));
        return repository.save(site);
    }

    public void delete(String siteId, String actor) {
        Site site = repository.findById(buildId(siteId))
            .filter(existing -> !isDeleted(existing.getDeleteMark()))
            .orElseThrow(() -> new NotFoundException("Site not found: " + siteId));

        site.setDeleteMark(DELETE_MARK_DELETED);
        site.setUpdatedAt(LocalDateTime.now());
        site.setUpdatedBy(resolveActor(actor));
        repository.save(site);
    }

    private SiteId buildId(String siteId) {
        return new SiteId(MemberUserDetailsService.DEFAULT_COMPANY, siteId);
    }

    private boolean isDeleted(String deleteMark) {
        return DELETE_MARK_DELETED.equalsIgnoreCase(deleteMark);
    }

    private String resolveActor(String actor) {
        return (actor == null || actor.isBlank()) ? DEFAULT_ACTOR : actor;
    }
}
