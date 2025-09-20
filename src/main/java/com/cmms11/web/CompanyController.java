package com.cmms11.web;

import com.cmms11.domain.company.CompanyRequest;
import com.cmms11.domain.company.CompanyResponse;
import com.cmms11.domain.company.CompanyService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/domain/companies")
public class CompanyController {

    private final CompanyService service;

    public CompanyController(CompanyService service) {
        this.service = service;
    }

    @GetMapping
    public Page<CompanyResponse> list(@RequestParam(name = "q", required = false) String q, Pageable pageable) {
        return service.list(q, pageable).map(CompanyResponse::from);
    }

    @GetMapping("/{id}")
    public CompanyResponse get(@PathVariable String id) {
        return CompanyResponse.from(service.get(id));
    }

    @PostMapping
    public ResponseEntity<CompanyResponse> create(@Valid @RequestBody CompanyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(CompanyResponse.from(service.create(request, currentActor())));
    }

    @PutMapping("/{id}")
    public CompanyResponse update(@PathVariable String id, @Valid @RequestBody CompanyRequest request) {
        return CompanyResponse.from(service.update(id, request, currentActor()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id, currentActor());
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
