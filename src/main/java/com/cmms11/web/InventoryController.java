package com.cmms11.web;

import java.util.Map;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventories")
public class InventoryController {

    @GetMapping
    public ResponseEntity<?> list(@RequestParam(name = "q", required = false) String q, Pageable pageable) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
            .body(Map.of("message", "list inventories not implemented"));
    }

    @GetMapping("/{inventoryId}")
    public ResponseEntity<?> get(@PathVariable String id) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
            .body(Map.of("message", "get inventory not implemented"));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
            .body(Map.of("message", "create inventory not implemented"));
    }

    @PutMapping("/{inventoryId}")
    public ResponseEntity<?> update(@PathVariable String id, @RequestBody Map<String, Object> body) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
            .body(Map.of("message", "update inventory not implemented"));
    }

    @DeleteMapping("/{inventoryId}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
            .body(Map.of("message", "delete inventory not implemented"));
    }
}

