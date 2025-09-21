package com.cmms11.web;

import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.cmms11.config.SecurityConfig;
import com.cmms11.domain.company.CompanyResponse;
import com.cmms11.domain.company.CompanyService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/**
 * 이름: CompanyControllerTest
 * 작성자: codex
 * 작성일: 2025-08-20
 * 수정일:
 * 프로그램 개요: 회사 화면 라우트를 검증하는 MVC 테스트.
 */
@WebMvcTest(controllers = CompanyController.class)
@Import(SecurityConfig.class)
class CompanyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CompanyService companyService;

    @MockBean
    private UserDetailsService userDetailsService;

    @WithMockUser
    @Test
    void listViewRendersPageWithSearchKeyword() throws Exception {
        LocalDateTime now = LocalDateTime.of(2024, 1, 1, 9, 0);
        CompanyResponse response = new CompanyResponse("C0001", "샘플회사", "123-45-67890", "test@company.com", "02-1234-5678", "메모", "N", now, "tester", now, "tester");
        Page<CompanyResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 20), 1);
        when(companyService.list(eq("샘플"), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/domain/company/list").param("q", "샘플"))
            .andExpect(status().isOk())
            .andExpect(view().name("domain/company/list"))
            .andExpect(model().attribute("page", sameInstance(page)))
            .andExpect(model().attribute("keyword", "샘플"));

        verify(companyService).list(eq("샘플"), any(Pageable.class));
    }

    @WithMockUser
    @Test
    void newFormProvidesDefaultModelAttributes() throws Exception {
        mockMvc.perform(get("/domain/company/form"))
            .andExpect(status().isOk())
            .andExpect(view().name("domain/company/form"))
            .andExpect(model().attribute("isNew", true))
            .andExpect(model().attributeExists("company"));

        verify(companyService, never()).get(anyString());
        verify(companyService, never()).list(anyString(), any(Pageable.class));
    }

    @WithMockUser
    @Test
    void editFormLoadsCompanyFromService() throws Exception {
        LocalDateTime now = LocalDateTime.of(2024, 1, 1, 9, 0);
        CompanyResponse response = new CompanyResponse("C0001", "샘플회사", "123-45-67890", "test@company.com", "02-1234-5678", "메모", "N", now, "tester", now, "tester");
        when(companyService.get("C0001")).thenReturn(response);

        mockMvc.perform(get("/domain/company/edit/{companyId}", "C0001"))
            .andExpect(status().isOk())
            .andExpect(view().name("domain/company/form"))
            .andExpect(model().attribute("company", response))
            .andExpect(model().attribute("isNew", false));

        verify(companyService).get("C0001");
    }
}