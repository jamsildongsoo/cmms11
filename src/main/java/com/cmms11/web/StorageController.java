package com.cmms11.web;

import com.cmms11.domain.storage.StorageRequest;
import com.cmms11.domain.storage.StorageResponse;
import com.cmms11.domain.storage.StorageService;
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
 * 이름: StorageController
 * 작성자: codex
 * 작성일: 2025-08-20
 * 수정일:
 * 프로그램 개요: 창고(Storage) 기준정보 API 컨트롤러.
 */
@RestController
@RequestMapping("/api/domain/storages")
public class StorageController {

    private final StorageService service;

    public StorageController(StorageService service) {
        this.service = service;
    }

    @GetMapping
    public Page<StorageResponse> list(@RequestParam(name = "q", required = false) String q, Pageable pageable) {
        return service.list(q, pageable);
    }

    @GetMapping("/{storageId}")
    public ResponseEntity<StorageResponse> get(@PathVariable String storageId) {
        return ResponseEntity.ok(service.get(storageId));
    }

    @PostMapping
    public ResponseEntity<StorageResponse> create(@Valid @RequestBody StorageRequest request) {
        StorageResponse response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{storageId}")
    public ResponseEntity<StorageResponse> update(
        @PathVariable String storageId,
        @Valid @RequestBody StorageRequest request
    ) {
        StorageResponse response = service.update(storageId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{storageId}")
    public ResponseEntity<Void> delete(@PathVariable String storageId) {
        service.delete(storageId);
        return ResponseEntity.noContent().build();
    }
}

