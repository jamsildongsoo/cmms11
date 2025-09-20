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
import com.cmms11.inventory.InventoryRequest;
import com.cmms11.inventory.InventoryResponse;
import com.cmms11.inventory.InventoryService;
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
 * 이름: InventoryControllerTest
 * 작성자: codex
 * 작성일: 2025-08-20
 * 수정일:
 * 프로그램 개요: InventoryController REST 엔드포인트의 동작을 검증하는 MVC 테스트.
 */
@WebMvcTest(controllers = InventoryController.class)
@Import(SecurityConfig.class)
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InventoryService inventoryService;

    @MockBean
    private UserDetailsService userDetailsService;

    @WithMockUser
    @Test
    void listInventoriesReturnsPagedResult() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        InventoryResponse response = sampleResponse(now);
        when(inventoryService.list(anyString(), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(response), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/inventories").param("q", "베어링"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].inventoryId").value("2000000001"))
            .andExpect(jsonPath("$.content[0].name").value("베어링"));

        verify(inventoryService).list(anyString(), any(Pageable.class));
    }

    @WithMockUser
    @Test
    void getInventoryReturnsSuccess() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        InventoryResponse response = sampleResponse(now);
        when(inventoryService.get("2000000001")).thenReturn(response);

        mockMvc.perform(get("/api/inventories/{inventoryId}", "2000000001"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.inventoryId").value("2000000001"))
            .andExpect(jsonPath("$.name").value("베어링"));

        verify(inventoryService).get("2000000001");
    }

    @WithMockUser
    @Test
    void createInventoryReturnsCreatedResponse() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        InventoryRequest request = new InventoryRequest(
            null,
            "베어링",
            "ASSET",
            "D0001",
            "SKF",
            "6205ZZ",
            "MODEL-01",
            "SN-001",
            "FG0001",
            "예비 부품",
            "ACTIVE"
        );
        InventoryResponse response = sampleResponse(now);
        when(inventoryService.create(any(InventoryRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/inventories")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.inventoryId").value("2000000001"));

        verify(inventoryService).create(any(InventoryRequest.class));
    }

    @WithMockUser
    @Test
    void updateInventoryReturnsOkResponse() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        InventoryRequest request = new InventoryRequest(
            "2000000001",
            "베어링-수정",
            "ASSET",
            "D0002",
            "SKF",
            "6205ZZ",
            "MODEL-02",
            "SN-002",
            "FG0001",
            "사양 변경",
            "ACTIVE"
        );
        InventoryResponse response = new InventoryResponse(
            "2000000001",
            "베어링-수정",
            "ASSET",
            "D0002",
            "SKF",
            "6205ZZ",
            "MODEL-02",
            "SN-002",
            "FG0001",
            "사양 변경",
            "ACTIVE",
            "N",
            now,
            "tester",
            now,
            "tester"
        );
        when(inventoryService.update(anyString(), any(InventoryRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/inventories/{inventoryId}", "2000000001")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("베어링-수정"));

        verify(inventoryService).update(anyString(), any(InventoryRequest.class));
    }

    @WithMockUser
    @Test
    void deleteInventoryReturnsNoContent() throws Exception {
        doNothing().when(inventoryService).delete("2000000001");

        mockMvc.perform(delete("/api/inventories/{inventoryId}", "2000000001").with(csrf()))
            .andExpect(status().isNoContent());

        verify(inventoryService).delete("2000000001");
    }

    private InventoryResponse sampleResponse(LocalDateTime timestamp) {
        return new InventoryResponse(
            "2000000001",
            "베어링",
            "ASSET",
            "D0001",
            "SKF",
            "6205ZZ",
            "MODEL-01",
            "SN-001",
            "FG0001",
            "예비 부품",
            "ACTIVE",
            "N",
            timestamp,
            "tester",
            timestamp,
            "tester"
        );
    }
}
