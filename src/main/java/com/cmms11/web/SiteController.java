package com.cmms11.web;

import java.util.Map;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/domain/sites")
public class SiteController {

    @GetMapping
    public ResponseEntity<?> list(@RequestParam(name = "q", required = false) String q, Pageable pageable) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
            .body(Map.of("message", "list sites not implemented"));
    }

    @GetMapping("/{siteId}")
    public ResponseEntity<?> get(@PathVariable String id) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
            .body(Map.of("message", "get site not implemented"));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
            .body(Map.of("message", "create site not implemented"));
    }

    @PutMapping("/{siteId}")
    public ResponseEntity<?> update(@PathVariable String id, @RequestBody Map<String, Object> body) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
            .body(Map.of("message", "update site not implemented"));
    }

    @DeleteMapping("/{siteId}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
            .body(Map.of("message", "delete site not implemented"));
    }
}

