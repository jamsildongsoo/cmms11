package com.cmms11.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cmms11.domain.member.Member;
import com.cmms11.domain.member.MemberId;
import com.cmms11.domain.member.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void meEndpointReturnsMemberInformation() throws Exception {
        MemberId id = new MemberId("C0001", "admin");
        Member member = new Member();
        member.setId(id);
        member.setName("관리자");
        member.setDeptId("D001");
        member.setEmail("admin@example.com");
        memberRepository.save(member);

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberId").value("admin"))
                .andExpect(jsonPath("$.companyId").value("C0001"))
                .andExpect(jsonPath("$.name").value("관리자"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_ADMIN"));
    }
}
