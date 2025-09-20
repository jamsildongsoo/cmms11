package com.cmms11.web;

import com.cmms11.workorder.WorkOrderRequest;
import com.cmms11.workorder.WorkOrderResponse;
import com.cmms11.workorder.WorkOrderService;
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
 * 이름: WorkOrderController
 * 작성자: codex
 * 작성일: 2025-08-20
 * 수정일:
 * 프로그램 개요: 작업지시 REST API 컨트롤러.
 */
@RestController
@RequestMapping("/api/workorders")
public class WorkOrderController {

    private final WorkOrderService service;

    public WorkOrderController(WorkOrderService service) {
        this.service = service;
    }

    @GetMapping
    public Page<WorkOrderResponse> list(@RequestParam(name = "q", required = false) String q, Pageable pageable) {
        return service.list(q, pageable);
    }

    @GetMapping("/{workOrderId}")
    public ResponseEntity<WorkOrderResponse> get(@PathVariable String workOrderId) {
        return ResponseEntity.ok(service.get(workOrderId));
    }

    @PostMapping
    public ResponseEntity<WorkOrderResponse> create(@Valid @RequestBody WorkOrderRequest request) {
        WorkOrderResponse response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{workOrderId}")
    public ResponseEntity<WorkOrderResponse> update(
        @PathVariable String workOrderId,
        @Valid @RequestBody WorkOrderRequest request
    ) {
        WorkOrderResponse response = service.update(workOrderId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{workOrderId}")
    public ResponseEntity<Void> delete(@PathVariable String workOrderId) {
        service.delete(workOrderId);
        return ResponseEntity.noContent().build();
    }
}
