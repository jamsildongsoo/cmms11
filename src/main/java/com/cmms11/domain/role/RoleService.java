package com.cmms11.domain.role;

import com.cmms11.common.error.NotFoundException;
import com.cmms11.security.MemberUserDetailsService;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 이름: RoleService
 * 작성자: codex
 * 작성일: 2025-08-20
 * 수정일:
 * 프로그램 개요: 권한(Role) 기준정보의 CRUD 비즈니스 로직 처리.
 */
@Service
@Transactional
public class RoleService {

    private final RoleRepository repository;

    public RoleService(RoleRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Page<RoleResponse> list(String keyword, Pageable pageable) {
        String companyId = MemberUserDetailsService.DEFAULT_COMPANY;
        Page<Role> page;
        if (keyword == null || keyword.isBlank()) {
            page = repository.findByIdCompanyIdAndDeleteMark(companyId, "N", pageable);
        } else {
            page = repository.searchByKeyword(companyId, "N", keyword.trim(), pageable);
        }
        return page.map(RoleResponse::from);
    }

    @Transactional(readOnly = true)
    public RoleResponse get(String roleId) {
        return RoleResponse.from(getActiveRole(roleId));
    }

    public RoleResponse create(RoleRequest request) {
        String companyId = MemberUserDetailsService.DEFAULT_COMPANY;
        Optional<Role> existing = repository.findByIdCompanyIdAndIdRoleId(companyId, request.roleId());
        LocalDateTime now = LocalDateTime.now();
        String memberId = currentMemberId();

        if (existing.isPresent()) {
            Role role = existing.get();
            if (!"Y".equalsIgnoreCase(role.getDeleteMark())) {
                throw new IllegalArgumentException("Role already exists: " + request.roleId());
            }
            role.setName(request.name());
            role.setNote(request.note());
            role.setDeleteMark("N");
            role.setUpdatedAt(now);
            role.setUpdatedBy(memberId);
            return RoleResponse.from(repository.save(role));
        }

        Role role = new Role();
        role.setId(new RoleId(companyId, request.roleId()));
        role.setName(request.name());
        role.setNote(request.note());
        role.setDeleteMark("N");
        role.setCreatedAt(now);
        role.setCreatedBy(memberId);
        role.setUpdatedAt(now);
        role.setUpdatedBy(memberId);
        return RoleResponse.from(repository.save(role));
    }

    public RoleResponse update(String roleId, RoleRequest request) {
        Role existing = getActiveRole(roleId);
        existing.setName(request.name());
        existing.setNote(request.note());
        existing.setUpdatedAt(LocalDateTime.now());
        existing.setUpdatedBy(currentMemberId());
        return RoleResponse.from(repository.save(existing));
    }

    public void delete(String roleId) {
        Role existing = getActiveRole(roleId);
        existing.setDeleteMark("Y");
        existing.setUpdatedAt(LocalDateTime.now());
        existing.setUpdatedBy(currentMemberId());
        repository.save(existing);
    }

    private Role getActiveRole(String roleId) {
        return repository.findByIdCompanyIdAndIdRoleId(MemberUserDetailsService.DEFAULT_COMPANY, roleId)
            .filter(role -> !"Y".equalsIgnoreCase(role.getDeleteMark()))
            .orElseThrow(() -> new NotFoundException("Role not found: " + roleId));
    }

    private String currentMemberId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "system";
        }
        String name = authentication.getName();
        return name != null ? name : "system";
    }
}
