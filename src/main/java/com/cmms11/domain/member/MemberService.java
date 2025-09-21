package com.cmms11.domain.member;

import com.cmms11.common.error.NotFoundException;
import com.cmms11.security.MemberUserDetailsService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class MemberService {
    private final MemberRepository repository;
    private final PasswordEncoder passwordEncoder;

    public MemberService(MemberRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public Page<Member> list(String q, Pageable pageable) {
        String companyId = MemberUserDetailsService.DEFAULT_COMPANY;
        if (q == null || q.isBlank()) {
            return repository.findByIdCompanyIdAndDeleteMark(companyId, "N", pageable);
        }
        return repository.search(companyId, "N", "%" + q + "%", pageable);
    }

    @Transactional(readOnly = true)
    public Member get(String memberId) {
        return repository.findByIdCompanyIdAndIdMemberId(MemberUserDetailsService.DEFAULT_COMPANY, memberId)
                .orElseThrow(() -> new NotFoundException("Member not found: " + memberId));
    }

    public Member create(Member member, String rawPassword, String actor) {
        String companyId = MemberUserDetailsService.DEFAULT_COMPANY;
        if (member.getId() == null) {
            throw new IllegalArgumentException("member.id (memberId) is required");
        }
        member.getId().setCompanyId(companyId);
        if (member.getDeleteMark() == null) {
            member.setDeleteMark("N");
        }
        if (rawPassword != null && !rawPassword.isBlank()) {
            member.setPasswordHash(passwordEncoder.encode(rawPassword));
        }
        LocalDateTime now = LocalDateTime.now();
        String actorId = actor != null ? actor : currentMemberId();
        member.setCreatedAt(now);
        member.setCreatedBy(actorId);
        member.setUpdatedAt(now);
        member.setUpdatedBy(actorId);
        return repository.save(member);
    }

    public Member update(Member member, String rawPassword, String actor) {
        if (member.getId() == null || member.getId().getMemberId() == null) {
            throw new IllegalArgumentException("member.id.memberId is required");
        }
        Member existing = get(member.getId().getMemberId());
        existing.setName(member.getName());
        existing.setDeptId(member.getDeptId());
        existing.setEmail(member.getEmail());
        existing.setPhone(member.getPhone());
        existing.setNote(member.getNote());
        if (member.getDeleteMark() != null) {
            existing.setDeleteMark(member.getDeleteMark());
        }
        if (rawPassword != null && !rawPassword.isBlank()) {
            existing.setPasswordHash(passwordEncoder.encode(rawPassword));
        }
        existing.setUpdatedAt(LocalDateTime.now());
        existing.setUpdatedBy(actor != null ? actor : currentMemberId());
        return repository.save(existing);
    }

    public void delete(String memberId) {
        Member existing = get(memberId);
        existing.setDeleteMark("Y");
        existing.setUpdatedAt(LocalDateTime.now());
        existing.setUpdatedBy(currentMemberId());
        repository.save(existing);
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
