package com.cmms11.web;

import com.cmms11.code.CodeItemRequest;
import com.cmms11.code.CodeItemResponse;
import com.cmms11.code.CodeService;
import com.cmms11.code.CodeTypeRequest;
import com.cmms11.code.CodeTypeResponse;
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
 * 이름: CodeController
 * 작성자: codex
 * 작성일: 2025-08-20
 * 수정일:
 * 프로그램 개요: 공통 코드 타입 및 항목을 관리하는 REST 컨트롤러.
 */
@RestController
@RequestMapping("/api/codes")
public class CodeController {

    private final CodeService service;

    public CodeController(CodeService service) {
        this.service = service;
    }

    @GetMapping("/types")
    public Page<CodeTypeResponse> listTypes(@RequestParam(name = "q", required = false) String q, Pageable pageable) {
        return service.listTypes(q, pageable);
    }

    @GetMapping("/types/{codeType}")
    public ResponseEntity<CodeTypeResponse> getType(@PathVariable String codeType) {
        return ResponseEntity.ok(service.getType(codeType));
    }

    @PostMapping("/types")
    public ResponseEntity<CodeTypeResponse> createType(@Valid @RequestBody CodeTypeRequest request) {
        CodeTypeResponse response = service.createType(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/types/{codeType}")
    public ResponseEntity<CodeTypeResponse> updateType(
        @PathVariable String codeType,
        @Valid @RequestBody CodeTypeRequest request
    ) {
        CodeTypeResponse response = service.updateType(codeType, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/types/{codeType}")
    public ResponseEntity<Void> deleteType(@PathVariable String codeType) {
        service.deleteType(codeType);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/types/{codeType}/items")
    public Page<CodeItemResponse> listItems(
        @PathVariable String codeType,
        @RequestParam(name = "q", required = false) String q,
        Pageable pageable
    ) {
        return service.listItems(codeType, q, pageable);
    }

    @GetMapping("/types/{codeType}/items/{code}")
    public ResponseEntity<CodeItemResponse> getItem(@PathVariable String codeType, @PathVariable String code) {
        return ResponseEntity.ok(service.getItem(codeType, code));
    }

    @PostMapping("/types/{codeType}/items")
    public ResponseEntity<CodeItemResponse> createItem(
        @PathVariable String codeType,
        @Valid @RequestBody CodeItemRequest request
    ) {
        CodeItemRequest payload = new CodeItemRequest(codeType, request.code(), request.name(), request.note());
        CodeItemResponse response = service.createItem(payload);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/types/{codeType}/items/{code}")
    public ResponseEntity<CodeItemResponse> updateItem(
        @PathVariable String codeType,
        @PathVariable String code,
        @Valid @RequestBody CodeItemRequest request
    ) {
        CodeItemRequest payload = new CodeItemRequest(codeType, code, request.name(), request.note());
        CodeItemResponse response = service.updateItem(codeType, code, payload);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/types/{codeType}/items/{code}")
    public ResponseEntity<Void> deleteItem(@PathVariable String codeType, @PathVariable String code) {
        service.deleteItem(codeType, code);
        return ResponseEntity.noContent().build();
    }
}

