package com.cmms11.domain.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.cmms11.common.error.UnauthorizedException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@ExtendWith(MockitoExtension.class)
class MemberAuthServiceTest {

    @Mock
    private MemberRepository memberRepository;

    private MemberAuthService memberAuthService;

    @BeforeEach
    void setUp() {
        memberAuthService = new MemberAuthService(memberRepository);
    }

    @Test
    void getAuthenticatedMemberReturnsResponseWhenAuthenticated() {
        MemberId id = new MemberId("C0001", "tester");
        Member member = new Member();
        member.setId(id);
        member.setName("홍길동");
        member.setDeptId("D100");
        member.setEmail("tester@example.com");

        when(memberRepository.findByIdCompanyIdAndIdMemberId(eq("C0001"), eq("tester")))
                .thenReturn(Optional.of(member));

        UsernamePasswordAuthenticationToken authentication = UsernamePasswordAuthenticationToken.authenticated(
                "tester",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        MemberAuthResponse response = memberAuthService.getAuthenticatedMember(authentication);

        assertThat(response.memberId()).isEqualTo("tester");
        assertThat(response.companyId()).isEqualTo("C0001");
        assertThat(response.roles()).containsExactly("ROLE_USER");
        assertThat(response.name()).isEqualTo("홍길동");
        assertThat(response.deptId()).isEqualTo("D100");
        assertThat(response.email()).isEqualTo("tester@example.com");
    }

    @Test
    void getAuthenticatedMemberThrowsWhenAuthenticationInvalid() {
        AnonymousAuthenticationToken anonymousAuthenticationToken = new AnonymousAuthenticationToken(
                "key",
                "anonymousUser",
                List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
        );

        assertThrows(UnauthorizedException.class,
                () -> memberAuthService.getAuthenticatedMember(anonymousAuthenticationToken));
    }

    @Test
    void getAuthenticatedMemberThrowsWhenAuthenticationMissing() {
        assertThrows(UnauthorizedException.class, () -> memberAuthService.getAuthenticatedMember(null));
    }
}
