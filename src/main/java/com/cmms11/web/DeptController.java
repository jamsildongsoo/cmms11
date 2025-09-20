package com.cmms11.web;

import com.cmms11.domain.dept.DeptCreateRequest;
import com.cmms11.domain.dept.DeptResponse;
import com.cmms11.domain.dept.DeptService;
import com.cmms11.domain.dept.DeptUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/domain/depts")
public class DeptController {

    private final DeptService service;

    public DeptController(DeptService service) {
        this.service = service;
    }

    @GetMapping
    public Page<DeptResponse> list(@RequestParam(name = "q", required = false) String q, Pageable pageable) {
        return service.list(q, pageable).map(DeptResponse::from);
    }

    @GetMapping("/{deptId}")
    public DeptResponse get(@PathVariable("deptId") String deptId) {
        return DeptResponse.from(service.get(deptId));
    }

    @PostMapping
    public ResponseEntity<DeptResponse> create(@Valid @RequestBody DeptCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(DeptResponse.from(service.create(request, currentActor())));
    }

    @PutMapping("/{deptId}")
    public DeptResponse update(@PathVariable("deptId") String deptId, @Valid @RequestBody DeptUpdateRequest request) {
        return DeptResponse.from(service.update(deptId, request, currentActor()));
    }

    @DeleteMapping("/{deptId}")
    public ResponseEntity<Void> delete(@PathVariable("deptId") String deptId) {
        service.delete(deptId, currentActor());
        return ResponseEntity.noContent().build();
    }

    private String currentActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof String principalName && "anonymousUser".equals(principalName)) {
            return null;
        }
        return authentication.getName();
    }
}
