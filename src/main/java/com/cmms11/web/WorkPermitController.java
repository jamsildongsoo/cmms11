package com.cmms11.web;

import com.cmms11.workpermit.WorkPermitRequest;
import com.cmms11.workpermit.WorkPermitResponse;
import com.cmms11.workpermit.WorkPermitService;
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
 * 이름: WorkPermitController
 * 작성자: codex
 * 작성일: 2025-08-20
 * 수정일:
 * 프로그램 개요: 작업허가 REST API 컨트롤러.
 */
@RestController
@RequestMapping("/api/workpermits")
public class WorkPermitController {

    private final WorkPermitService service;

    public WorkPermitController(WorkPermitService service) {
        this.service = service;
    }

    @GetMapping
    public Page<WorkPermitResponse> list(@RequestParam(name = "q", required = false) String q, Pageable pageable) {
        return service.list(q, pageable);
    }

    @GetMapping("/{workPermitId}")
    public ResponseEntity<WorkPermitResponse> get(@PathVariable String workPermitId) {
        return ResponseEntity.ok(service.get(workPermitId));
    }

    @PostMapping
    public ResponseEntity<WorkPermitResponse> create(@Valid @RequestBody WorkPermitRequest request) {
        WorkPermitResponse response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{workPermitId}")
    public ResponseEntity<WorkPermitResponse> update(
        @PathVariable String workPermitId,
        @Valid @RequestBody WorkPermitRequest request
    ) {
        WorkPermitResponse response = service.update(workPermitId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{workPermitId}")
    public ResponseEntity<Void> delete(@PathVariable String workPermitId) {
        service.delete(workPermitId);
        return ResponseEntity.noContent().build();
    }
}
