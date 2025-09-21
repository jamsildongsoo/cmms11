package com.cmms11.web;

import com.cmms11.domain.member.MemberAuthResponse;
import com.cmms11.domain.member.MemberAuthService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final MemberAuthService memberAuthService;

    public AuthController(MemberAuthService memberAuthService) {
        this.memberAuthService = memberAuthService;
    }

    @GetMapping("/me")
    public MemberAuthResponse me(Authentication authentication) {
        return memberAuthService.getAuthenticatedMember(authentication);
    }
}

