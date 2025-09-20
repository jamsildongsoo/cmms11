package com.cmms11.web;

import java.util.Map;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inspections")
public class InspectionController {

    @GetMapping
    public ResponseEntity<?> list(@RequestParam(name = "q", required = false) String q, Pageable pageable) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
            .body(Map.of("message", "list inspections not implemented"));
    }

    @GetMapping("/{inspectionId}")
    public ResponseEntity<?> get(@PathVariable String id) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
            .body(Map.of("message", "get inspection not implemented"));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
            .body(Map.of("message", "create inspection not implemented"));
    }

    @PutMapping("/{inspectionId}")
    public ResponseEntity<?> update(@PathVariable String id, @RequestBody Map<String, Object> body) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
            .body(Map.of("message", "update inspection not implemented"));
    }

    @DeleteMapping("/{inspectionId}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
            .body(Map.of("message", "delete inspection not implemented"));
    }
}

