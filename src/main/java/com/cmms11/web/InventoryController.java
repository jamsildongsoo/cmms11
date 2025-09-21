package com.cmms11.web;

import com.cmms11.common.upload.BulkUploadResult;
import com.cmms11.inventory.InventoryRequest;
import com.cmms11.inventory.InventoryResponse;
import com.cmms11.inventory.InventoryService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 이름: InventoryController
 * 작성자: codex
 * 작성일: 2025-08-20
 * 수정일:
 * 프로그램 개요: 재고 마스터 CRUD REST 엔드포인트를 제공하는 컨트롤러.
 */
@RestController
@RequestMapping("/api/inventories")
public class InventoryController {

    private final InventoryService service;

    public InventoryController(InventoryService service) {
        this.service = service;
    }

    @GetMapping
    public Page<InventoryResponse> list(@RequestParam(name = "q", required = false) String q, Pageable pageable) {
        return service.list(q, pageable);
    }

    @GetMapping("/{inventoryId}")
    public ResponseEntity<InventoryResponse> get(@PathVariable String inventoryId) {
        return ResponseEntity.ok(service.get(inventoryId));
    }

    @PostMapping
    public ResponseEntity<InventoryResponse> create(@Valid @RequestBody InventoryRequest request) {
        InventoryResponse response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{inventoryId}")
    public ResponseEntity<InventoryResponse> update(
        @PathVariable String inventoryId,
        @Valid @RequestBody InventoryRequest request
    ) {
        InventoryResponse response = service.update(inventoryId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{inventoryId}")
    public ResponseEntity<Void> delete(@PathVariable String inventoryId) {
        service.delete(inventoryId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BulkUploadResult> upload(@RequestParam("file") MultipartFile file) {
        BulkUploadResult result = service.upload(file);
        return ResponseEntity.ok(result);
    }
}
