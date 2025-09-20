package com.cmms11.web;

import com.cmms11.domain.storage.Storage;
import com.cmms11.domain.storage.StorageService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/domain/storages")
public class StorageController {
    private final StorageService service;

    public StorageController(StorageService service) {
        this.service = service;
    }

    @GetMapping
    public Page<Storage> list(@RequestParam(name = "q", required = false) String q, Pageable pageable) {
        return service.list(q, pageable);
    }

    @GetMapping("/{storageId}")
    public ResponseEntity<Storage> get(@PathVariable String storageId) {
        return ResponseEntity.ok(service.get(storageId));
    }

    @PostMapping
    public ResponseEntity<Storage> create(@Valid @RequestBody Storage storage) {
        Storage saved = service.create(storage);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{storageId}")
    public ResponseEntity<Storage> update(@PathVariable String storageId, @Valid @RequestBody Storage storage) {
        Storage updated = service.update(storageId, storage);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{storageId}")
    public ResponseEntity<Void> delete(@PathVariable String storageId) {
        service.delete(storageId);
        return ResponseEntity.noContent().build();
    }
}
