package com.cmms11.web;

import com.cmms11.domain.dept.DeptRequest;
import com.cmms11.domain.dept.DeptResponse;
import com.cmms11.domain.dept.DeptService;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 이름: DeptController
 * 작성자: codex
 * 작성일: 2025-08-20
 * 수정일:
 * 프로그램 개요: 부서 기준정보 API 컨트롤러.
 */
@RestController
@RequestMapping("/api/domain/depts")
public class DeptController {

    private final DeptService service;

    public DeptController(DeptService service) {
        this.service = service;
    }

    @GetMapping
    public Page<DeptResponse> list(@RequestParam(name = "q", required = false) String q, Pageable pageable) {

        return service.list(q, pageable);
    }

    @GetMapping("/{deptId}")
    public ResponseEntity<DeptResponse> get(@PathVariable String deptId) {
        return ResponseEntity.ok(service.get(deptId));
    }

    @PostMapping
    public ResponseEntity<DeptResponse> create(@Valid @RequestBody DeptRequest request) {
        DeptResponse response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{deptId}")
    public ResponseEntity<DeptResponse> update(
        @PathVariable String deptId,
        @Valid @RequestBody DeptRequest request
    ) {
        DeptResponse response = service.update(deptId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{deptId}")
    public ResponseEntity<Void> delete(@PathVariable String deptId) {
        service.delete(deptId);
        return ResponseEntity.noContent().build();
    }
}
