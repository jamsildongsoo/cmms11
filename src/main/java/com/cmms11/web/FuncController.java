package com.cmms11.web;

import com.cmms11.domain.func.Func;
import com.cmms11.domain.func.FuncService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/domain/funcs")
public class FuncController {
    private final FuncService service;

    public FuncController(FuncService service) {
        this.service = service;
    }

    @GetMapping
    public Page<Func> list(@RequestParam(name = "q", required = false) String q, Pageable pageable) {
        return service.list(q, pageable);
    }

    @GetMapping("/{funcId}")
    public ResponseEntity<Func> get(@PathVariable String funcId) {
        return ResponseEntity.ok(service.get(funcId));
    }

    @PostMapping
    public ResponseEntity<Func> create(@Valid @RequestBody Func func) {
        Func saved = service.create(func);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{funcId}")
    public ResponseEntity<Func> update(@PathVariable String funcId, @Valid @RequestBody Func func) {
        Func updated = service.update(funcId, func);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{funcId}")
    public ResponseEntity<Void> delete(@PathVariable String funcId) {
        service.delete(funcId);
        return ResponseEntity.noContent().build();
    }
}
