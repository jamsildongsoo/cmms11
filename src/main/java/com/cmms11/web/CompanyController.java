package com.cmms11.web;

import com.cmms11.domain.company.CompanyRequest;
import com.cmms11.domain.company.CompanyResponse;
import com.cmms11.domain.company.CompanyService;
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
 * 이름: CompanyController
 * 작성자: codex
 * 작성일: 2025-08-20
 * 수정일:
 * 프로그램 개요: 회사 기준정보 API 컨트롤러.
 */
@RestController
@RequestMapping("/api/domain/companies")
public class CompanyController {

    private final CompanyService service;

    public CompanyController(CompanyService service) {
        this.service = service;
    }

    @GetMapping
    public Page<CompanyResponse> list(@RequestParam(name = "q", required = false) String q, Pageable pageable) {
        return service.list(q, pageable);
    }

    @GetMapping("/{companyId}")
    public ResponseEntity<CompanyResponse> get(@PathVariable String companyId) {
        return ResponseEntity.ok(service.get(companyId));
    }

    @PostMapping
    public ResponseEntity<CompanyResponse> create(@Valid @RequestBody CompanyRequest request) {
        CompanyResponse response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{companyId}")
    public ResponseEntity<CompanyResponse> update(
        @PathVariable String companyId,
        @Valid @RequestBody CompanyRequest request
    ) {
        CompanyResponse response = service.update(companyId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{companyId}")
    public ResponseEntity<Void> delete(@PathVariable String companyId) {
        service.delete(companyId);
        return ResponseEntity.noContent().build();
    }
}

