package com.cmms11.security;

import com.cmms11.domain.member.Member;
import com.cmms11.domain.member.MemberRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MemberUserDetailsService implements UserDetailsService {
    public static final String DEFAULT_COMPANY = "C0001";

    private final MemberRepository memberRepository;

    public MemberUserDetailsService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String companyId = DEFAULT_COMPANY;
        String memberId = username;
        if (username != null && username.contains(":")) {
            String[] parts = username.split(":", 2);
            companyId = parts[0];
            memberId = parts[1];
        }

        Member m = memberRepository.findByIdCompanyIdAndIdMemberId(companyId, memberId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        boolean enabled = (m.getDeleteMark() == null || !"Y".equalsIgnoreCase(m.getDeleteMark()));
        List<GrantedAuthority> auths = new ArrayList<>();
        if ("admin".equalsIgnoreCase(memberId)) {
            auths.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        } else {
            auths.add(new SimpleGrantedAuthority("ROLE_USER"));
        }
        return new User(memberId, m.getPasswordHash(), enabled, true, true, true, auths);
    }
}

