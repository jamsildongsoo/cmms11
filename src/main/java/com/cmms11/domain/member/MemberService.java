package com.cmms11.domain.member;

import com.cmms11.common.error.NotFoundException;
import com.cmms11.security.MemberUserDetailsService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
        if (member.getDeleteMark() == null) member.setDeleteMark("N");
        if (rawPassword != null && !rawPassword.isBlank()) {
            member.setPasswordHash(passwordEncoder.encode(rawPassword));
        }
        member.setCreatedAt(LocalDateTime.now());
        member.setCreatedBy(actor != null ? actor : "system");
        return repository.save(member);
    }
}
