package com.cmms11.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
import com.cmms11.inspection.InspectionItemRequest;
import com.cmms11.inspection.InspectionItemResponse;
import com.cmms11.inspection.InspectionRequest;
import com.cmms11.inspection.InspectionResponse;
import com.cmms11.inspection.InspectionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
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

@WebMvcTest(controllers = InspectionController.class)
@Import(SecurityConfig.class)
class InspectionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InspectionService inspectionService;

    @MockBean
    private UserDetailsService userDetailsService;

    @WithMockUser
    @Test
    void listInspectionsReturnsPagedResult() throws Exception {
        InspectionResponse response = sampleResponse(LocalDateTime.now());
        when(inspectionService.list(anyString(), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(response), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/inspections").param("q", "정기점검"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].inspectionId").value("I250101001"))
            .andExpect(jsonPath("$.content[0].items").isArray());

        verify(inspectionService).list(anyString(), any(Pageable.class));
    }

    @WithMockUser
    @Test
    void getInspectionReturnsSuccess() throws Exception {
        InspectionResponse response = sampleResponse(LocalDateTime.now());
        when(inspectionService.get("I250101001")).thenReturn(response);

        mockMvc.perform(get("/api/inspections/{inspectionId}", "I250101001"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.inspectionId").value("I250101001"))
            .andExpect(jsonPath("$.items[0].unit").value("bar"));

        verify(inspectionService).get("I250101001");
    }

    @WithMockUser
    @Test
    void createInspectionReturnsCreatedResponse() throws Exception {
        InspectionRequest request = sampleRequest();
        InspectionResponse response = sampleResponse(LocalDateTime.now());
        when(inspectionService.create(any(InspectionRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/inspections")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.inspectionId").value("I250101001"));

        verify(inspectionService).create(any(InspectionRequest.class));
    }

    @WithMockUser
    @Test
    void updateInspectionReturnsOkResponse() throws Exception {
        InspectionRequest request = sampleRequest();
        InspectionResponse response = sampleResponse(LocalDateTime.now());
        when(inspectionService.update(anyString(), any(InspectionRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/inspections/{inspectionId}", "I250101001")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items[0].name").value("압력"));

        verify(inspectionService).update(anyString(), any(InspectionRequest.class));
    }

    @WithMockUser
    @Test
    void deleteInspectionReturnsNoContent() throws Exception {
        doNothing().when(inspectionService).delete("I250101001");

        mockMvc.perform(delete("/api/inspections/{inspectionId}", "I250101001").with(csrf()))
            .andExpect(status().isNoContent());

        verify(inspectionService).delete("I250101001");
    }

    private InspectionRequest sampleRequest() {
        return new InspectionRequest(
            null,
            "정기점검",
            "P0001",
            "J0001",
            "S0001",
            "D0001",
            "M0001",
            LocalDate.of(2025, 1, 1),
            LocalDate.of(2025, 1, 2),
            "READY",
            "FG0001",
            "특이사항",
            List.of(new InspectionItemRequest("압력", "방법", "1", "2", "1.5", "bar", "1.60", "비고"))
        );
    }

    private InspectionResponse sampleResponse(LocalDateTime now) {
        return new InspectionResponse(
            "I250101001",
            "정기점검",
            "P0001",
            "J0001",
            "S0001",
            "D0001",
            "M0001",
            LocalDate.of(2025, 1, 1),
            LocalDate.of(2025, 1, 2),
            "READY",
            "FG0001",
            "특이사항",
            now,
            "tester",
            now,
            "tester",
            List.of(new InspectionItemResponse(1, "압력", "방법", "1", "2", "1.5", "bar", "1.60", "비고"))
        );
    }
}
