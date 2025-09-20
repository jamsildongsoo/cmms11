package com.cmms11.web;

import com.cmms11.inspection.InspectionRequest;
import com.cmms11.inspection.InspectionResponse;
import com.cmms11.inspection.InspectionService;
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
 * 이름: InspectionController
 * 작성자: codex
 * 작성일: 2025-08-20
 * 수정일:
 * 프로그램 개요: 예방점검 REST API 컨트롤러.
 */
@RestController
@RequestMapping("/api/inspections")
public class InspectionController {

    private final InspectionService service;

    public InspectionController(InspectionService service) {
        this.service = service;
    }

    @GetMapping
    public Page<InspectionResponse> list(@RequestParam(name = "q", required = false) String q, Pageable pageable) {
        return service.list(q, pageable);
    }

    @GetMapping("/{inspectionId}")
    public ResponseEntity<InspectionResponse> get(@PathVariable String inspectionId) {
        return ResponseEntity.ok(service.get(inspectionId));
    }

    @PostMapping
    public ResponseEntity<InspectionResponse> create(@Valid @RequestBody InspectionRequest request) {
        InspectionResponse response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{inspectionId}")
    public ResponseEntity<InspectionResponse> update(
        @PathVariable String inspectionId,
        @Valid @RequestBody InspectionRequest request
    ) {
        InspectionResponse response = service.update(inspectionId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{inspectionId}")
    public ResponseEntity<Void> delete(@PathVariable String inspectionId) {
        service.delete(inspectionId);
        return ResponseEntity.noContent().build();
    }
}
