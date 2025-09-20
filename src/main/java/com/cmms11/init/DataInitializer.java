package com.cmms11.init;

import com.cmms11.domain.member.Member;
import com.cmms11.domain.member.MemberId;
import com.cmms11.domain.member.MemberRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements ApplicationRunner {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // Seed admin user for company C0001 with password 1234 (BCrypt)
        MemberId adminId = new MemberId("C0001", "admin");
        if (!memberRepository.existsById(adminId)) {
            Member admin = new Member();
            admin.setId(adminId);
            admin.setName("Administrator");
            admin.setDeptId("ADMIN");
            admin.setPasswordHash(passwordEncoder.encode("1234"));
            admin.setDeleteMark("N");
            admin.setCreatedAt(LocalDateTime.now());
            admin.setCreatedBy("system");
            memberRepository.save(admin);
        }
    }
}

