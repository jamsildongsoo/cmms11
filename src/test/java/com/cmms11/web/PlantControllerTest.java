package com.cmms11.web;

import com.cmms11.config.SecurityConfig;
import com.cmms11.plant.Plant;
import com.cmms11.plant.PlantId;
import com.cmms11.plant.PlantRequest;
import com.cmms11.plant.PlantResponse;
import com.cmms11.plant.PlantService;
import com.cmms11.code.CodeService;
import com.cmms11.domain.site.SiteService;
import com.cmms11.domain.dept.DeptService;
import com.cmms11.domain.dept.DeptResponse;
import com.cmms11.domain.site.SiteResponse;
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

import java.math.BigDecimal;
import java.time.LocalDate;
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

@WebMvcTest(controllers = PlantController.class)
@Import(SecurityConfig.class)
class PlantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PlantService plantService;

    @MockBean
    private CodeService codeService;

    @MockBean
    private SiteService siteService;

    @MockBean
    private DeptService deptService;

    @MockBean
    private UserDetailsService userDetailsService;

    @Test
    @WithMockUser
    void listView_정상조회() throws Exception {
        // Given
        PlantId plantId = new PlantId(MemberUserDetailsService.DEFAULT_COMPANY, "P0001");
        Plant plant = new Plant();
        plant.setId(plantId);
        plant.setName("테스트 설비");
        plant.setAssetId("A0001");
        plant.setSiteId("S0001");
        plant.setDeptId("EQ");
        plant.setFuncId("F0001");
        plant.setMakerName("테스트 제조사");
        plant.setModel("테스트 모델");
        plant.setInstallDate(LocalDate.of(2024, 1, 1));
        plant.setStatus("ACTIVE");
        plant.setDeleteMark("N");

        Page<PlantResponse> page = new PageImpl<>(List.of(PlantResponse.from(plant)), PageRequest.of(0, 20), 1);
        when(plantService.list(anyString(), any(Pageable.class))).thenReturn(page);

        DeptResponse dept = new DeptResponse("EQ", "설비과", MemberUserDetailsService.DEFAULT_COMPANY, "02-1234-5678", "서울시", null, "ACTIVE", "설비 관리", "N", 
                LocalDateTime.now(), "admin", LocalDateTime.now(), "admin");
        when(deptService.list(any(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(dept)));

        // When & Then
        mockMvc.perform(get("/plant/list"))
                .andExpect(status().isOk())
                .andExpect(view().name("plant/list"))
                .andExpect(model().attributeExists("page"))
                .andExpect(model().attributeExists("keyword"))
                .andExpect(model().attributeExists("depts"));

        verify(plantService).list(anyString(), any(Pageable.class));
        verify(deptService).list(any(), any(Pageable.class));
    }

    @Test
    @WithMockUser
    void listView_검색어포함조회() throws Exception {
        // Given
        PlantId plantId = new PlantId(MemberUserDetailsService.DEFAULT_COMPANY, "P0001");
        Plant plant = new Plant();
        plant.setId(plantId);
        plant.setName("테스트 설비");

        Page<PlantResponse> page = new PageImpl<>(List.of(PlantResponse.from(plant)), PageRequest.of(0, 20), 1);
        when(plantService.list(eq("테스트"), any(Pageable.class))).thenReturn(page);
        when(deptService.list(any(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        // When & Then
        mockMvc.perform(get("/plant/list").param("q", "테스트"))
                .andExpect(status().isOk())
                .andExpect(view().name("plant/list"))
                .andExpect(model().attribute("keyword", "테스트"));

        verify(plantService).list(eq("테스트"), any(Pageable.class));
    }

    @Test
    @WithMockUser
    void newForm_새설비등록폼() throws Exception {
        // Given
        when(codeService.listItems(anyString(), anyString(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));
        when(siteService.list(anyString(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));
        when(deptService.list(anyString(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        // When & Then
        mockMvc.perform(get("/plant/form"))
                .andExpect(status().isOk())
                .andExpect(view().name("plant/form"))
                .andExpect(model().attribute("isNew", true))
                .andExpect(model().attributeExists("plant"))
                .andExpect(model().attributeExists("assetTypes"))
                .andExpect(model().attributeExists("depreTypes"))
                .andExpect(model().attributeExists("sites"))
                .andExpect(model().attributeExists("depts"))
                .andExpect(model().attributeExists("funcs"));

        verify(plantService, never()).get(anyString());
    }

    @Test
    @WithMockUser
    void editForm_설비수정폼() throws Exception {
        // Given
        PlantId plantId = new PlantId(MemberUserDetailsService.DEFAULT_COMPANY, "P0001");
        Plant plant = new Plant();
        plant.setId(plantId);
        plant.setName("테스트 설비");
        plant.setAssetId("A0001");
        plant.setSiteId("S0001");
        plant.setDeptId("EQ");
        plant.setFuncId("F0001");
        plant.setMakerName("테스트 제조사");
        plant.setModel("테스트 모델");
        plant.setInstallDate(LocalDate.of(2024, 1, 1));
        plant.setStatus("ACTIVE");
        plant.setDeleteMark("N");

        PlantResponse plantResponse = PlantResponse.from(plant);
        when(plantService.get("P0001")).thenReturn(plantResponse);
        when(codeService.listItems(anyString(), anyString(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));
        when(siteService.list(anyString(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));
        when(deptService.list(anyString(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        // When & Then
        mockMvc.perform(get("/plant/edit/{plantId}", "P0001"))
                .andExpect(status().isOk())
                .andExpect(view().name("plant/form"))
                .andExpect(model().attribute("isNew", false))
                .andExpect(model().attributeExists("plant"))
                .andExpect(model().attributeExists("assetTypes"))
                .andExpect(model().attributeExists("depreTypes"))
                .andExpect(model().attributeExists("sites"))
                .andExpect(model().attributeExists("depts"))
                .andExpect(model().attributeExists("funcs"));

        verify(plantService).get("P0001");
    }

    @Test
    @WithMockUser
    void uploadForm_일괄등록폼() throws Exception {
        // When & Then
        mockMvc.perform(get("/plant/uploadForm"))
                .andExpect(status().isOk())
                .andExpect(view().name("plant/uploadForm"));
    }

    @Test
    @WithMockUser
    void save_새설비등록() throws Exception {
        // Given
        PlantResponse plantResponse = new PlantResponse(
                "P0001", "테스트 설비", "A0001", "S0001", "EQ", "F0001",
                "테스트 제조사", "테스트 스펙", "테스트 모델", "SERIAL001",
                LocalDate.of(2024, 1, 1), "D0001", 10,
                new BigDecimal("1000000"), new BigDecimal("100000"),
                "Y", "N", "Y", 30, LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 2, 1), "FG0001", "테스트 메모",
                "ACTIVE", "N", LocalDateTime.now(), "admin",
                LocalDateTime.now(), "admin"
        );
        when(plantService.create(any(PlantRequest.class))).thenReturn(plantResponse);

        // When & Then
        mockMvc.perform(post("/plant/save")
                        .param("plantId", "P0001")
                        .param("name", "테스트 설비")
                        .param("assetId", "A0001")
                        .param("siteId", "S0001")
                        .param("deptId", "EQ")
                        .param("funcId", "F0001")
                        .param("makerName", "테스트 제조사")
                        .param("spec", "테스트 스펙")
                        .param("model", "테스트 모델")
                        .param("serial", "SERIAL001")
                        .param("installDate", "2024-01-01")
                        .param("depreId", "D0001")
                        .param("deprePeriod", "10")
                        .param("purchaseCost", "1000000")
                        .param("residualValue", "100000")
                        .param("inspectionYn", "Y")
                        .param("psmYn", "N")
                        .param("workpermitYn", "Y")
                        .param("inspectionInterval", "30")
                        .param("lastInspection", "2024-01-01")
                        .param("nextInspection", "2024-02-01")
                        .param("fileGroupId", "FG0001")
                        .param("note", "테스트 메모")
                        .param("status", "ACTIVE")
                        .param("isNew", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/plant/list"));

        verify(plantService).create(any(PlantRequest.class));
    }

    @Test
    @WithMockUser
    void save_설비수정() throws Exception {
        // Given
        PlantResponse plantResponse = new PlantResponse(
                "P0001", "수정된 설비", "A0001", "S0001", "EQ", "F0001",
                "수정된 제조사", "수정된 스펙", "수정된 모델", "SERIAL001",
                LocalDate.of(2024, 1, 1), "D0001", 10,
                new BigDecimal("1000000"), new BigDecimal("100000"),
                "Y", "N", "Y", 30, LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 2, 1), "FG0001", "수정된 메모",
                "ACTIVE", "N", LocalDateTime.now(), "admin",
                LocalDateTime.now(), "admin"
        );
        when(plantService.update(anyString(), any(PlantRequest.class))).thenReturn(plantResponse);

        // When & Then
        mockMvc.perform(post("/plant/save")
                        .param("plantId", "P0001")
                        .param("name", "수정된 설비")
                        .param("assetId", "A0001")
                        .param("siteId", "S0001")
                        .param("deptId", "EQ")
                        .param("funcId", "F0001")
                        .param("makerName", "수정된 제조사")
                        .param("spec", "수정된 스펙")
                        .param("model", "수정된 모델")
                        .param("serial", "SERIAL001")
                        .param("installDate", "2024-01-01")
                        .param("depreId", "D0001")
                        .param("deprePeriod", "10")
                        .param("purchaseCost", "1000000")
                        .param("residualValue", "100000")
                        .param("inspectionYn", "Y")
                        .param("psmYn", "N")
                        .param("workpermitYn", "Y")
                        .param("inspectionInterval", "30")
                        .param("lastInspection", "2024-01-01")
                        .param("nextInspection", "2024-02-01")
                        .param("fileGroupId", "FG0001")
                        .param("note", "수정된 메모")
                        .param("status", "ACTIVE")
                        .param("isNew", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/plant/list"));

        verify(plantService).update(eq("P0001"), any(PlantRequest.class));
    }

    @Test
    @WithMockUser
    void delete_설비삭제() throws Exception {
        // Given
        doNothing().when(plantService).delete(anyString());

        // When & Then
        mockMvc.perform(post("/plant/delete/{plantId}", "P0001"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/plant/list"));

        verify(plantService).delete(eq("P0001"));
    }

    @Test
    @WithMockUser
    void apiList_정상조회() throws Exception {
        // Given
        PlantId plantId = new PlantId(MemberUserDetailsService.DEFAULT_COMPANY, "P0001");
        Plant plant = new Plant();
        plant.setId(plantId);
        plant.setName("테스트 설비");

        Page<PlantResponse> page = new PageImpl<>(List.of(PlantResponse.from(plant)), PageRequest.of(0, 20), 1);
        when(plantService.list(anyString(), any(Pageable.class))).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/plants"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name", is("테스트 설비")));

        verify(plantService).list(anyString(), any(Pageable.class));
    }

    @Test
    @WithMockUser
    void apiGet_정상조회() throws Exception {
        // Given
        PlantId plantId = new PlantId(MemberUserDetailsService.DEFAULT_COMPANY, "P0001");
        Plant plant = new Plant();
        plant.setId(plantId);
        plant.setName("테스트 설비");
        plant.setAssetId("A0001");
        plant.setSiteId("S0001");
        plant.setDeptId("EQ");

        PlantResponse plantResponse = PlantResponse.from(plant);
        when(plantService.get("P0001")).thenReturn(plantResponse);

        // When & Then
        mockMvc.perform(get("/api/plants/{plantId}", "P0001"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name", is("테스트 설비")))
                .andExpect(jsonPath("$.assetId", is("A0001")))
                .andExpect(jsonPath("$.siteId", is("S0001")))
                .andExpect(jsonPath("$.deptId", is("EQ")));

        verify(plantService).get("P0001");
    }

    @Test
    @WithMockUser
    void apiCreate_정상생성() throws Exception {
        // Given
        PlantId plantId = new PlantId(MemberUserDetailsService.DEFAULT_COMPANY, "P0001");
        Plant plant = new Plant();
        plant.setId(plantId);
        plant.setName("테스트 설비");
        plant.setAssetId("A0001");
        plant.setSiteId("S0001");
        plant.setDeptId("EQ");

        PlantResponse plantResponse = PlantResponse.from(plant);
        when(plantService.create(any(PlantRequest.class))).thenReturn(plantResponse);

        // When & Then
        mockMvc.perform(post("/api/plants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "plantId": "P0001",
                                    "name": "테스트 설비",
                                    "assetId": "A0001",
                                    "siteId": "S0001",
                                    "deptId": "EQ",
                                    "funcId": "F0001",
                                    "makerName": "테스트 제조사",
                                    "spec": "테스트 스펙",
                                    "model": "테스트 모델",
                                    "serial": "SERIAL001",
                                    "installDate": "2024-01-01",
                                    "depreId": "D0001",
                                    "deprePeriod": 10,
                                    "purchaseCost": 1000000,
                                    "residualValue": 100000,
                                    "inspectionYn": "Y",
                                    "psmYn": "N",
                                    "workpermitYn": "Y",
                                    "inspectionInterval": 30,
                                    "lastInspection": "2024-01-01",
                                    "nextInspection": "2024-02-01",
                                    "fileGroupId": "FG0001",
                                    "note": "테스트 메모",
                                    "status": "ACTIVE"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name", is("테스트 설비")))
                .andExpect(jsonPath("$.assetId", is("A0001")));

        verify(plantService).create(any(PlantRequest.class));
    }
}
