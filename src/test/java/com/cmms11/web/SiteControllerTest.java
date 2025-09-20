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

import com.cmms11.domain.site.Site;
import com.cmms11.domain.site.SiteCreateRequest;
import com.cmms11.domain.site.SiteId;
import com.cmms11.domain.site.SiteService;
import com.cmms11.domain.site.SiteUpdateRequest;
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

@WebMvcTest(SiteController.class)
@AutoConfigureMockMvc(addFilters = false)
class SiteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SiteService service;

    @Test
    void listSitesDelegatesToService() throws Exception {
        Site site = new Site();
        site.setId(new SiteId("C0001", "S0001"));
        site.setName("Main Site");
        site.setDeleteMark("N");
        Page<Site> page = new PageImpl<>(List.of(site), PageRequest.of(0, 20), 1);

        when(service.list(eq("Main"), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/domain/sites").param("q", "Main"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].siteId").value("S0001"))
            .andExpect(jsonPath("$.content[0].name").value("Main Site"));

        verify(service).list(eq("Main"), any(Pageable.class));
    }

    @Test
    void getSiteReturnsEntity() throws Exception {
        Site site = new Site();
        site.setId(new SiteId("C0001", "S0001"));
        site.setName("Main Site");
        site.setDeleteMark("N");

        when(service.get("S0001")).thenReturn(site);

        mockMvc.perform(get("/api/domain/sites/S0001"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.siteId").value("S0001"))
            .andExpect(jsonPath("$.name").value("Main Site"));

        verify(service).get("S0001");
    }

    @Test
    void createSiteReturnsCreated() throws Exception {
        Site site = new Site();
        site.setId(new SiteId("C0001", "S0001"));
        site.setName("Main Site");
        site.setDeleteMark("N");

        when(service.create(any(SiteCreateRequest.class), isNull())).thenReturn(site);

        mockMvc.perform(post("/api/domain/sites")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"siteId\":\"S0001\",\"name\":\"Main Site\",\"note\":\"Primary\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.siteId").value("S0001"))
            .andExpect(jsonPath("$.name").value("Main Site"));

        verify(service).create(any(SiteCreateRequest.class), isNull());
    }

    @Test
    void updateSiteReturnsUpdated() throws Exception {
        Site site = new Site();
        site.setId(new SiteId("C0001", "S0001"));
        site.setName("Updated Site");
        site.setDeleteMark("N");

        when(service.update(eq("S0001"), any(SiteUpdateRequest.class), isNull())).thenReturn(site);

        mockMvc.perform(put("/api/domain/sites/S0001")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Updated Site\",\"note\":\"Updated\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.siteId").value("S0001"))
            .andExpect(jsonPath("$.name").value("Updated Site"));

        verify(service).update(eq("S0001"), any(SiteUpdateRequest.class), isNull());
    }

    @Test
    void deleteSiteReturnsNoContent() throws Exception {
        doNothing().when(service).delete(anyString(), any());

        mockMvc.perform(delete("/api/domain/sites/S0001"))
            .andExpect(status().isNoContent());

        verify(service).delete(eq("S0001"), isNull());
    }
}
