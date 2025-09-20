package com.cmms11.domain.func;

import com.cmms11.common.error.NotFoundException;
import com.cmms11.security.MemberUserDetailsService;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class FuncService {
    private final FuncRepository repository;

    public FuncService(FuncRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Page<Func> list(String q, Pageable pageable) {
        String companyId = MemberUserDetailsService.DEFAULT_COMPANY;
        if (q == null || q.isBlank()) {
            return repository.findByIdCompanyIdAndDeleteMark(companyId, "N", pageable);
        }
        return repository.search(companyId, "N", "%" + q + "%", pageable);
    }

    @Transactional(readOnly = true)
    public Func get(String funcId) {
        FuncId id = new FuncId(MemberUserDetailsService.DEFAULT_COMPANY, funcId);
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Func not found: " + funcId));
    }

    public Func create(Func func) {
        if (func.getId() == null || func.getId().getFuncId() == null || func.getId().getFuncId().isBlank()) {
            throw new IllegalArgumentException("func.id.funcId is required");
        }
        String companyId = MemberUserDetailsService.DEFAULT_COMPANY;
        func.getId().setCompanyId(companyId);
        if (func.getDeleteMark() == null) {
            func.setDeleteMark("N");
        }
        LocalDateTime now = LocalDateTime.now();
        if (func.getCreatedAt() == null) {
            func.setCreatedAt(now);
        }
        if (func.getUpdatedAt() == null) {
            func.setUpdatedAt(now);
        }
        return repository.save(func);
    }

    public Func update(String funcId, Func func) {
        String companyId = MemberUserDetailsService.DEFAULT_COMPANY;
        FuncId id = new FuncId(companyId, funcId);
        Func existing = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Func not found: " + funcId));
        func.setId(id);
        if (func.getDeleteMark() == null) {
            func.setDeleteMark(existing.getDeleteMark());
        }
        if (func.getCreatedAt() == null) {
            func.setCreatedAt(existing.getCreatedAt());
        }
        if (func.getCreatedBy() == null) {
            func.setCreatedBy(existing.getCreatedBy());
        }
        if (func.getUpdatedBy() == null) {
            func.setUpdatedBy(existing.getUpdatedBy());
        }
        func.setUpdatedAt(LocalDateTime.now());
        return repository.save(func);
    }

    public void delete(String funcId) {
        String companyId = MemberUserDetailsService.DEFAULT_COMPANY;
        FuncId id = new FuncId(companyId, funcId);
        Func existing = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Func not found: " + funcId));
        existing.setDeleteMark("Y");
        existing.setUpdatedAt(LocalDateTime.now());
        repository.save(existing);
    }
}
