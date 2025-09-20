package com.cmms11.web;

import com.cmms11.domain.site.SiteRequest;
import com.cmms11.domain.site.SiteResponse;
import com.cmms11.domain.site.SiteService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 이름: SiteController
 * 작성자: codex
 * 작성일: 2025-08-20
 * 수정일:
 * 프로그램 개요: 사업장 기준정보 API 컨트롤러.
 */
@RestController
@RequestMapping("/api/domain/sites")
public class SiteController {

    private final SiteService service;

    public SiteController(SiteService service) {
        this.service = service;
    }

    @GetMapping
    public Page<SiteResponse> list(@RequestParam(name = "q", required = false) String q, Pageable pageable) {
        return service.list(q, pageable);
    }

    @GetMapping("/{siteId}")
    public ResponseEntity<SiteResponse> get(@PathVariable String siteId) {
        return ResponseEntity.ok(service.get(siteId));
    }

    @PostMapping
    public ResponseEntity<SiteResponse> create(@Valid @RequestBody SiteRequest request) {
        SiteResponse response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{siteId}")
    public ResponseEntity<SiteResponse> update(
        @PathVariable String siteId,
        @Valid @RequestBody SiteRequest request
    ) {
        SiteResponse response = service.update(siteId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{siteId}")
    public ResponseEntity<Void> delete(@PathVariable String siteId) {
        service.delete(siteId);
        return ResponseEntity.noContent().build();
    }
}

