package com.cmms11.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cmms11.config.SecurityConfig;
import com.cmms11.domain.company.CompanyRequest;
import com.cmms11.domain.company.CompanyResponse;
import com.cmms11.domain.company.CompanyService;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/**
 * 이름: CompanyControllerTest
 * 작성자: codex
 * 작성일: 2025-08-20
 * 수정일:
 * 프로그램 개요: CompanyController REST 엔드포인트에 대한 MVC 레이어 테스트.
 */
@WebMvcTest(controllers = CompanyController.class)
@Import(SecurityConfig.class)
class CompanyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CompanyService companyService;

    @MockBean
    private UserDetailsService userDetailsService;

    @WithMockUser
    @Test
    void listCompaniesReturnsPagedResponse() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        CompanyResponse response = new CompanyResponse("C0001", "Sample Company", "memo", "N", now, "tester", now, "tester");
        when(companyService.list(anyString(), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(response), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/domain/companies").param("q", "Sample"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].companyId").value("C0001"))
            .andExpect(jsonPath("$.content[0].name").value("Sample Company"));

        verify(companyService).list(eq("Sample"), any(Pageable.class));
    }

    @WithMockUser
    @Test
    void createCompanyReturnsCreatedResponse() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        CompanyRequest request = new CompanyRequest("C1000", "New Company", "memo");
        CompanyResponse response = new CompanyResponse("C1000", "New Company", "memo", "N", now, "tester", now, "tester");
        when(companyService.create(any(CompanyRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/domain/companies")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.companyId").value("C1000"))
            .andExpect(jsonPath("$.name").value("New Company"));

        verify(companyService).create(any(CompanyRequest.class));
    }

    @WithMockUser
    @Test
    void updateCompanyReturnsOkResponse() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        CompanyRequest request = new CompanyRequest("C1000", "Updated Company", "memo");
        CompanyResponse response = new CompanyResponse("C1000", "Updated Company", "memo", "N", now, "tester", now, "tester");
        when(companyService.update(eq("C1000"), any(CompanyRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/domain/companies/{companyId}", "C1000")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Updated Company"));

        verify(companyService).update(eq("C1000"), any(CompanyRequest.class));
    }

    @WithMockUser
    @Test
    void deleteCompanyReturnsNoContent() throws Exception {
        doNothing().when(companyService).delete("C1000");

        mockMvc.perform(delete("/api/domain/companies/{companyId}", "C1000").with(csrf()))
            .andExpect(status().isNoContent());

        verify(companyService).delete("C1000");
    }
}
