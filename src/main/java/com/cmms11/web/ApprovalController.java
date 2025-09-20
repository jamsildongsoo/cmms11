package com.cmms11.web;

import com.cmms11.approval.ApprovalRequest;
import com.cmms11.approval.ApprovalResponse;
import com.cmms11.approval.ApprovalService;
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
 * 이름: ApprovalController
 * 작성자: codex
 * 작성일: 2025-08-20
 * 수정일:
 * 프로그램 개요: 전자결재 REST API 컨트롤러.
 */
@RestController
@RequestMapping("/api/approvals")
public class ApprovalController {

    private final ApprovalService service;

    public ApprovalController(ApprovalService service) {
        this.service = service;
    }

    @GetMapping
    public Page<ApprovalResponse> list(@RequestParam(name = "q", required = false) String q, Pageable pageable) {
        return service.list(q, pageable);
    }

    @GetMapping("/{approvalId}")
    public ResponseEntity<ApprovalResponse> get(@PathVariable String approvalId) {
        return ResponseEntity.ok(service.get(approvalId));
    }

    @PostMapping
    public ResponseEntity<ApprovalResponse> create(@Valid @RequestBody ApprovalRequest request) {
        ApprovalResponse response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{approvalId}")
    public ResponseEntity<ApprovalResponse> update(
        @PathVariable String approvalId,
        @Valid @RequestBody ApprovalRequest request
    ) {
        ApprovalResponse response = service.update(approvalId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{approvalId}")
    public ResponseEntity<Void> delete(@PathVariable String approvalId) {
        service.delete(approvalId);
        return ResponseEntity.noContent().build();
    }
}
