package com.cmms11.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cmms11.domain.company.Company;
import com.cmms11.domain.company.CompanyRequest;
import com.cmms11.domain.company.CompanyService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CompanyController.class)
@AutoConfigureMockMvc(addFilters = false)
class CompanyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CompanyService service;

    @Test
    void listCompaniesDelegatesToService() throws Exception {
        Company company = new Company();
        company.setCompanyId("C0001");
        company.setName("Acme");
        company.setDeleteMark("N");
        Page<Company> page = new PageImpl<>(List.of(company), PageRequest.of(0, 20), 1);

        when(service.list(eq("Ac"), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/domain/companies").param("q", "Ac"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].companyId").value("C0001"))
            .andExpect(jsonPath("$.content[0].name").value("Acme"));

        verify(service).list(eq("Ac"), any(Pageable.class));
    }

    @Test
    void getCompanyReturnsEntity() throws Exception {
        Company company = new Company();
        company.setCompanyId("C0001");
        company.setName("Acme");
        company.setDeleteMark("N");
        company.setCreatedAt(LocalDateTime.now());

        when(service.get("C0001")).thenReturn(company);

        mockMvc.perform(get("/api/domain/companies/C0001"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.companyId").value("C0001"))
            .andExpect(jsonPath("$.name").value("Acme"));

        verify(service).get("C0001");
    }

    @Test
    void createCompanyReturnsCreated() throws Exception {
        Company company = new Company();
        company.setCompanyId("C0001");
        company.setName("Acme");
        company.setDeleteMark("N");

        when(service.create(any(CompanyRequest.class), isNull())).thenReturn(company);

        mockMvc.perform(post("/api/domain/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Acme\",\"note\":\"Note\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.companyId").value("C0001"))
            .andExpect(jsonPath("$.name").value("Acme"));

        verify(service).create(any(CompanyRequest.class), isNull());
    }

    @Test
    void updateCompanyReturnsUpdated() throws Exception {
        Company company = new Company();
        company.setCompanyId("C0001");
        company.setName("Updated");
        company.setDeleteMark("N");

        when(service.update(eq("C0001"), any(CompanyRequest.class), isNull())).thenReturn(company);

        mockMvc.perform(put("/api/domain/companies/C0001")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Updated\",\"note\":\"Note\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.companyId").value("C0001"))
            .andExpect(jsonPath("$.name").value("Updated"));

        verify(service).update(eq("C0001"), any(CompanyRequest.class), isNull());
    }

    @Test
    void deleteCompanyReturnsNoContent() throws Exception {
        doNothing().when(service).delete(anyString(), any());

        mockMvc.perform(delete("/api/domain/companies/C0001"))
            .andExpect(status().isNoContent());

        verify(service).delete(eq("C0001"), isNull());
    }
}
