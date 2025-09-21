package com.cmms11.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cmms11.common.error.UnauthorizedException;
import com.cmms11.config.SecurityConfig;
import com.cmms11.domain.member.MemberAuthResponse;
import com.cmms11.domain.member.MemberAuthService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MemberAuthService memberAuthService;

    @MockBean
    private UserDetailsService userDetailsService;

    @Test
    @WithMockUser(username = "tester", roles = {"USER"})
    void meReturnsAuthenticatedMemberInformation() throws Exception {
        MemberAuthResponse response = new MemberAuthResponse(
                "tester",
                "C0001",
                List.of("ROLE_USER"),
                "홍길동",
                "D100",
                "tester@example.com"
        );

        when(memberAuthService.getAuthenticatedMember(any(Authentication.class))).thenReturn(response);

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberId").value("tester"))
                .andExpect(jsonPath("$.companyId").value("C0001"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"))
                .andExpect(jsonPath("$.name").value("홍길동"))
                .andExpect(jsonPath("$.deptId").value("D100"))
                .andExpect(jsonPath("$.email").value("tester@example.com"));
    }

    @Test
    @WithMockUser
    void meReturnsUnauthorizedWhenServiceThrowsException() throws Exception {
        when(memberAuthService.getAuthenticatedMember(any(Authentication.class)))
                .thenThrow(new UnauthorizedException("Authentication is required"));

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Authentication is required"));
    }
}
