package com.cmms11.web;

import com.cmms11.domain.site.SiteCreateRequest;
import com.cmms11.domain.site.SiteResponse;
import com.cmms11.domain.site.SiteService;
import com.cmms11.domain.site.SiteUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/domain/sites")
public class SiteController {

    private final SiteService service;

    public SiteController(SiteService service) {
        this.service = service;
    }

    @GetMapping
    public Page<SiteResponse> list(@RequestParam(name = "q", required = false) String q, Pageable pageable) {
        return service.list(q, pageable).map(SiteResponse::from);
    }

    @GetMapping("/{siteId}")
    public SiteResponse get(@PathVariable("siteId") String siteId) {
        return SiteResponse.from(service.get(siteId));
    }

    @PostMapping
    public ResponseEntity<SiteResponse> create(@Valid @RequestBody SiteCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(SiteResponse.from(service.create(request, currentActor())));
    }

    @PutMapping("/{siteId}")
    public SiteResponse update(@PathVariable("siteId") String siteId, @Valid @RequestBody SiteUpdateRequest request) {
        return SiteResponse.from(service.update(siteId, request, currentActor()));
    }

    @DeleteMapping("/{siteId}")
    public ResponseEntity<Void> delete(@PathVariable("siteId") String siteId) {
        service.delete(siteId, currentActor());
        return ResponseEntity.noContent().build();
    }

    private String currentActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof String principalName && "anonymousUser".equals(principalName)) {
            return null;
        }
        return authentication.getName();
    }
}
