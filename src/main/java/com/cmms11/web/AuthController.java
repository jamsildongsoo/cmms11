package com.cmms11.web;

import com.cmms11.domain.member.Member;
import com.cmms11.domain.member.MemberRepository;
import com.cmms11.security.MemberUserDetailsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final MemberRepository memberRepository;

    public AuthController(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        String memberId = authentication.getName();
        String companyId = MemberUserDetailsService.DEFAULT_COMPANY;

        Member member = memberRepository
                .findByIdCompanyIdAndIdMemberId(companyId, memberId)
                .orElse(null);

        List<String> roles = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        Map<String, Object> body = new HashMap<>();
        body.put("memberId", memberId);
        body.put("companyId", companyId);
        body.put("roles", roles);
        if (member != null) {
            body.put("name", member.getName());
            body.put("deptId", member.getDeptId());
            body.put("email", member.getEmail());
        }

        return ResponseEntity.ok(body);
    }
}

