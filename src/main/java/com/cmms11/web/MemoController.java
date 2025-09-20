package com.cmms11.web;

import com.cmms11.memo.MemoRequest;
import com.cmms11.memo.MemoResponse;
import com.cmms11.memo.MemoService;
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
 * 이름: MemoController
 * 작성자: codex
 * 작성일: 2025-08-20
 * 수정일:
 * 프로그램 개요: 메모 REST API 컨트롤러.
 */
@RestController
@RequestMapping("/api/memos")
public class MemoController {

    private final MemoService service;

    public MemoController(MemoService service) {
        this.service = service;
    }

    @GetMapping
    public Page<MemoResponse> list(@RequestParam(name = "q", required = false) String q, Pageable pageable) {
        return service.list(q, pageable);
    }

    @GetMapping("/{memoId}")
    public ResponseEntity<MemoResponse> get(@PathVariable String memoId) {
        return ResponseEntity.ok(service.get(memoId));
    }

    @PostMapping
    public ResponseEntity<MemoResponse> create(@Valid @RequestBody MemoRequest request) {
        MemoResponse response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{memoId}")
    public ResponseEntity<MemoResponse> update(
        @PathVariable String memoId,
        @Valid @RequestBody MemoRequest request
    ) {
        MemoResponse response = service.update(memoId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{memoId}")
    public ResponseEntity<Void> delete(@PathVariable String memoId) {
        service.delete(memoId);
        return ResponseEntity.noContent().build();
    }
}
