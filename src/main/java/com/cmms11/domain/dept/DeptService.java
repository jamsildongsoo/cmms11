package com.cmms11.domain.dept;

import com.cmms11.common.error.NotFoundException;
import com.cmms11.security.MemberUserDetailsService;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DeptService {

    private static final String DELETE_MARK_ACTIVE = "N";
    private static final String DELETE_MARK_DELETED = "Y";
    private static final String DEFAULT_ACTOR = "system";

    private final DeptRepository repository;

    public DeptService(DeptRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Page<Dept> list(String q, Pageable pageable) {
        String companyId = MemberUserDetailsService.DEFAULT_COMPANY;
        if (q == null || q.isBlank()) {
            return repository.findByIdCompanyIdAndDeleteMark(companyId, DELETE_MARK_ACTIVE, pageable);
        }
        String like = "%" + q + "%";
        return repository.search(companyId, DELETE_MARK_ACTIVE, like, pageable);
    }

    @Transactional(readOnly = true)
    public Dept get(String deptId) {
        return repository.findById(buildId(deptId))
            .filter(existing -> !isDeleted(existing.getDeleteMark()))
            .orElseThrow(() -> new NotFoundException("Dept not found: " + deptId));
    }

    public Dept create(DeptCreateRequest request, String actor) {
        DeptId id = buildId(request.deptId());
        Dept dept = repository.findById(id).orElseGet(() -> {
            Dept fresh = new Dept();
            fresh.setId(id);
            return fresh;
        });

        if (!isDeleted(dept.getDeleteMark()) && dept.getCreatedAt() != null) {
            throw new IllegalStateException("Dept already exists: " + request.deptId());
        }

        dept.setName(request.name());
        dept.setNote(request.note());
        dept.setParentId(request.parentId());
        dept.setDeleteMark(DELETE_MARK_ACTIVE);
        LocalDateTime now = LocalDateTime.now();
        String resolvedActor = resolveActor(actor);
        dept.setCreatedAt(now);
        dept.setCreatedBy(resolvedActor);
        dept.setUpdatedAt(now);
        dept.setUpdatedBy(resolvedActor);
        return repository.save(dept);
    }

    public Dept update(String deptId, DeptUpdateRequest request, String actor) {
        Dept dept = repository.findById(buildId(deptId))
            .filter(existing -> !isDeleted(existing.getDeleteMark()))
            .orElseThrow(() -> new NotFoundException("Dept not found: " + deptId));

        dept.setName(request.name());
        dept.setNote(request.note());
        dept.setParentId(request.parentId());
        if (dept.getDeleteMark() == null) {
            dept.setDeleteMark(DELETE_MARK_ACTIVE);
        }
        dept.setUpdatedAt(LocalDateTime.now());
        dept.setUpdatedBy(resolveActor(actor));
        return repository.save(dept);
    }

    public void delete(String deptId, String actor) {
        Dept dept = repository.findById(buildId(deptId))
            .filter(existing -> !isDeleted(existing.getDeleteMark()))
            .orElseThrow(() -> new NotFoundException("Dept not found: " + deptId));

        dept.setDeleteMark(DELETE_MARK_DELETED);
        dept.setUpdatedAt(LocalDateTime.now());
        dept.setUpdatedBy(resolveActor(actor));
        repository.save(dept);
    }

    private DeptId buildId(String deptId) {
        return new DeptId(MemberUserDetailsService.DEFAULT_COMPANY, deptId);
    }

    private boolean isDeleted(String deleteMark) {
        return DELETE_MARK_DELETED.equalsIgnoreCase(deleteMark);
    }

    private String resolveActor(String actor) {
        return (actor == null || actor.isBlank()) ? DEFAULT_ACTOR : actor;
    }
}
