package com.cmms11.web;

import com.cmms11.domain.func.FuncRequest;
import com.cmms11.domain.func.FuncResponse;
import com.cmms11.domain.func.FuncService;
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
 * 이름: FuncController
 * 작성자: codex
 * 작성일: 2025-08-20
 * 수정일:
 * 프로그램 개요: 기능위치 기준정보 API 컨트롤러.
 */
@RestController
@RequestMapping("/api/domain/funcs")
public class FuncController {

    private final FuncService service;

    public FuncController(FuncService service) {
        this.service = service;
    }

    @GetMapping

    public Page<FuncResponse> list(@RequestParam(name = "q", required = false) String q, Pageable pageable) {
        return service.list(q, pageable);
    }

    @GetMapping("/{funcId}")

    public ResponseEntity<FuncResponse> get(@PathVariable String funcId) {

        return ResponseEntity.ok(service.get(funcId));
    }

    @PostMapping

    public ResponseEntity<FuncResponse> create(@Valid @RequestBody FuncRequest request) {
        FuncResponse response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{funcId}")
    public ResponseEntity<FuncResponse> update(
        @PathVariable String funcId,
        @Valid @RequestBody FuncRequest request
    ) {
        FuncResponse response = service.update(funcId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{funcId}")
    public ResponseEntity<Void> delete(@PathVariable String funcId) {
        service.delete(funcId);
        return ResponseEntity.noContent().build();
    }
}

