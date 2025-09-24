package com.cmms11.web;

import com.cmms11.config.SecurityConfig;
import com.cmms11.domain.member.Member;
import com.cmms11.domain.member.MemberId;
import com.cmms11.domain.member.MemberService;
import com.cmms11.domain.dept.DeptService;
import com.cmms11.domain.dept.DeptResponse;
import com.cmms11.security.MemberUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = MemberController.class)
@Import(SecurityConfig.class)
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MemberService memberService;

    @MockBean
    private DeptService deptService;

    @MockBean
    private UserDetailsService userDetailsService;

    @Test
    @WithMockUser
    void listView_정상조회() throws Exception {
        // Given
        MemberId memberId = new MemberId(MemberUserDetailsService.DEFAULT_COMPANY, "test01");
        Member member = new Member();
        member.setId(memberId);
        member.setName("테스트 사용자");
        member.setDeptId("EQ");
        member.setEmail("test@example.com");
        member.setPhone("010-1234-5678");
        member.setSiteId("S0001");
        member.setNote("테스트 메모");
        member.setDeleteMark("N");

        Page<Member> page = new PageImpl<>(List.of(member), PageRequest.of(0, 20), 1);
        when(memberService.list(anyString(), any(Pageable.class))).thenReturn(page);

        DeptResponse dept = new DeptResponse("EQ", "설비과", MemberUserDetailsService.DEFAULT_COMPANY, "02-1234-5678", "서울시", null, "ACTIVE", "설비 관리", "N", 
                LocalDateTime.now(), "admin", LocalDateTime.now(), "admin");
        when(deptService.list(any(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(dept)));

        // When & Then
        mockMvc.perform(get("/domain/member/list"))
                .andExpect(status().isOk())
                .andExpect(view().name("domain/member/list"))
                .andExpect(model().attributeExists("page"))
                .andExpect(model().attributeExists("keyword"))
                .andExpect(model().attributeExists("depts"));

        verify(memberService).list(anyString(), any(Pageable.class));
        verify(deptService).list(any(), any(Pageable.class));
    }

    @Test
    @WithMockUser
    void listView_검색어포함조회() throws Exception {
        // Given
        MemberId memberId = new MemberId(MemberUserDetailsService.DEFAULT_COMPANY, "test01");
        Member member = new Member();
        member.setId(memberId);
        member.setName("테스트 사용자");

        Page<Member> page = new PageImpl<>(List.of(member), PageRequest.of(0, 20), 1);
        when(memberService.list(eq("테스트"), any(Pageable.class))).thenReturn(page);
        when(deptService.list(any(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        // When & Then
        mockMvc.perform(get("/domain/member/list").param("q", "테스트"))
                .andExpect(status().isOk())
                .andExpect(view().name("domain/member/list"))
                .andExpect(model().attribute("keyword", "테스트"));

        verify(memberService).list(eq("테스트"), any(Pageable.class));
    }

    @Test
    @WithMockUser
    void newForm_새사용자등록폼() throws Exception {
        // Given
        when(deptService.list(any(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        // When & Then
        mockMvc.perform(get("/domain/member/form"))
                .andExpect(status().isOk())
                .andExpect(view().name("domain/member/form"))
                .andExpect(model().attribute("isNew", true))
                .andExpect(model().attributeExists("member"))
                .andExpect(model().attributeExists("depts"));

        verify(memberService, never()).get(anyString());
        verify(deptService).list(any(), any(Pageable.class));
    }

    @Test
    @WithMockUser
    void editForm_사용자수정폼() throws Exception {
        // Given
        MemberId memberId = new MemberId(MemberUserDetailsService.DEFAULT_COMPANY, "test01");
        Member member = new Member();
        member.setId(memberId);
        member.setName("테스트 사용자");
        member.setDeptId("EQ");
        member.setEmail("test@example.com");
        member.setPhone("010-1234-5678");
        member.setSiteId("S0001");
        member.setNote("테스트 메모");
        member.setDeleteMark("N");

        when(memberService.get("test01")).thenReturn(member);
        when(deptService.list(any(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        // When & Then
        mockMvc.perform(get("/domain/member/edit/{memberId}", "test01"))
                .andExpect(status().isOk())
                .andExpect(view().name("domain/member/form"))
                .andExpect(model().attribute("isNew", false))
                .andExpect(model().attributeExists("member"))
                .andExpect(model().attributeExists("depts"));

        verify(memberService).get("test01");
        verify(deptService).list(any(), any(Pageable.class));
    }

    @Test
    @WithMockUser
    void save_새사용자등록() throws Exception {
        // Given
        when(memberService.create(any(Member.class), anyString(), anyString())).thenReturn(new Member());

        // When & Then
        mockMvc.perform(post("/domain/member/save")
                        .param("memberId", "test01")
                        .param("name", "테스트 사용자")
                        .param("deptId", "EQ")
                        .param("email", "test@example.com")
                        .param("phone", "010-1234-5678")
                        .param("siteId", "S0001")
                        .param("note", "테스트 메모")
                        .param("password", "password123")
                        .param("deleteMark", "N")
                        .param("isNew", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/domain/member/list"));

        verify(memberService).create(any(Member.class), eq("password123"), anyString());
    }

    @Test
    @WithMockUser
    void save_사용자수정() throws Exception {
        // Given
        when(memberService.update(any(Member.class), anyString(), anyString())).thenReturn(new Member());

        // When & Then
        mockMvc.perform(post("/domain/member/save")
                        .param("memberId", "test01")
                        .param("name", "수정된 사용자")
                        .param("deptId", "AS")
                        .param("email", "updated@example.com")
                        .param("phone", "010-9999-9999")
                        .param("siteId", "S0002")
                        .param("note", "수정된 메모")
                        .param("password", "newpassword123")
                        .param("deleteMark", "N")
                        .param("isNew", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/domain/member/list"));

        verify(memberService).update(any(Member.class), eq("newpassword123"), anyString());
    }

    @Test
    @WithMockUser
    void delete_사용자삭제() throws Exception {
        // Given
        doNothing().when(memberService).delete(anyString(), anyString());

        // When & Then
        mockMvc.perform(post("/domain/member/delete/{memberId}", "test01"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/domain/member/list"));

        verify(memberService).delete(eq("test01"), anyString());
    }

    @Test
    @WithMockUser
    void apiList_정상조회() throws Exception {
        // Given
        MemberId memberId = new MemberId(MemberUserDetailsService.DEFAULT_COMPANY, "test01");
        Member member = new Member();
        member.setId(memberId);
        member.setName("테스트 사용자");

        Page<Member> page = new PageImpl<>(List.of(member), PageRequest.of(0, 20), 1);
        when(memberService.list(anyString(), any(Pageable.class))).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/members"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name", is("테스트 사용자")));

        verify(memberService).list(anyString(), any(Pageable.class));
    }

    @Test
    @WithMockUser
    void apiGet_정상조회() throws Exception {
        // Given
        MemberId memberId = new MemberId(MemberUserDetailsService.DEFAULT_COMPANY, "test01");
        Member member = new Member();
        member.setId(memberId);
        member.setName("테스트 사용자");
        member.setDeptId("EQ");
        member.setEmail("test@example.com");

        when(memberService.get("test01")).thenReturn(member);

        // When & Then
        mockMvc.perform(get("/api/members/{memberId}", "test01"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name", is("테스트 사용자")))
                .andExpect(jsonPath("$.deptId", is("EQ")))
                .andExpect(jsonPath("$.email", is("test@example.com")));

        verify(memberService).get("test01");
    }

    @Test
    @WithMockUser
    void apiCreate_정상생성() throws Exception {
        // Given
        MemberId memberId = new MemberId(MemberUserDetailsService.DEFAULT_COMPANY, "test01");
        Member member = new Member();
        member.setId(memberId);
        member.setName("테스트 사용자");
        member.setDeptId("EQ");
        member.setEmail("test@example.com");

        when(memberService.create(any(Member.class), anyString(), anyString())).thenReturn(member);

        // When & Then
        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "memberId": "test01",
                                    "name": "테스트 사용자",
                                    "deptId": "EQ",
                                    "email": "test@example.com",
                                    "phone": "010-1234-5678",
                                    "siteId": "S0001",
                                    "note": "테스트 메모",
                                    "password": "password123",
                                    "deleteMark": "N"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name", is("테스트 사용자")))
                .andExpect(jsonPath("$.deptId", is("EQ")));

        verify(memberService).create(any(Member.class), eq("password123"), anyString());
    }
}
