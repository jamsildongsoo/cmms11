package com.cmms11.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FileGroupIdApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String TEN_CHAR_FILE_GROUP_ID = "F20240101A";
    private static final String ELEVEN_CHAR_FILE_GROUP_ID = "F20240101AB";

    @Test
    @WithMockUser(username = "tester")
    void createPlantWithTenCharFileGroupId() throws Exception {
        Map<String, Object> payload = Map.of(
            "name", "Plant A",
            "fileGroupId", TEN_CHAR_FILE_GROUP_ID
        );

        postExpectCreated("/api/plants", payload, TEN_CHAR_FILE_GROUP_ID);
    }

    @Test
    @WithMockUser(username = "tester")
    void createInventoryWithTenCharFileGroupId() throws Exception {
        Map<String, Object> payload = Map.of(
            "name", "Inventory A",
            "assetId", "AS001",
            "deptId", "DP001",
            "fileGroupId", TEN_CHAR_FILE_GROUP_ID
        );

        postExpectCreated("/api/inventories", payload, TEN_CHAR_FILE_GROUP_ID);
    }

    @Test
    @WithMockUser(username = "tester")
    void createInspectionWithTenCharFileGroupId() throws Exception {
        Map<String, Object> payload = Map.of(
            "name", "Inspection A",
            "plantId", "P000000001",
            "jobId", "JOB01",
            "siteId", "ST001",
            "deptId", "DP001",
            "status", "DRAFT",
            "fileGroupId", TEN_CHAR_FILE_GROUP_ID
        );

        postExpectCreated("/api/inspections", payload, TEN_CHAR_FILE_GROUP_ID);
    }

    @Test
    @WithMockUser(username = "tester")
    void createWorkOrderWithTenCharFileGroupId() throws Exception {
        Map<String, Object> payload = Map.of(
            "name", "Work order A",
            "plantId", "P000000001",
            "jobId", "JOB01",
            "siteId", "ST001",
            "deptId", "DP001",
            "plannedDate", "2025-01-01",
            "status", "DRAFT",
            "fileGroupId", TEN_CHAR_FILE_GROUP_ID
        );

        postExpectCreated("/api/workorders", payload, TEN_CHAR_FILE_GROUP_ID);
    }

    @Test
    @WithMockUser(username = "tester")
    void createWorkPermitWithTenCharFileGroupId() throws Exception {
        Map<String, Object> payload = Map.of(
            "name", "Permit A",
            "plantId", "P000000001",
            "jobId", "JOB01",
            "siteId", "ST001",
            "deptId", "DP001",
            "status", "DRAFT",
            "fileGroupId", TEN_CHAR_FILE_GROUP_ID
        );

        postExpectCreated("/api/workpermits", payload, TEN_CHAR_FILE_GROUP_ID);
    }

    @Test
    @WithMockUser(username = "tester")
    void createMemoWithTenCharFileGroupId() throws Exception {
        Map<String, Object> payload = Map.of(
            "title", "Memo A",
            "content", "Test memo",
            "fileGroupId", TEN_CHAR_FILE_GROUP_ID
        );

        postExpectCreated("/api/memos", payload, TEN_CHAR_FILE_GROUP_ID);
    }

    @Test
    @WithMockUser(username = "tester")
    void createApprovalWithTenCharFileGroupId() throws Exception {
        Map<String, Object> payload = Map.of(
            "title", "Approval A",
            "status", "DRAFT",
            "refEntity", "WORK_ORDER",
            "refId", "O000000001",
            "content", "Approval body",
            "fileGroupId", TEN_CHAR_FILE_GROUP_ID,
            "steps", List.of()
        );

        postExpectCreated("/api/approvals", payload, TEN_CHAR_FILE_GROUP_ID);
    }

    @Test
    @WithMockUser(username = "tester")
    void rejectWorkOrderWhenFileGroupIdExceedsLimit() throws Exception {
        Map<String, Object> payload = Map.of(
            "name", "Invalid file group",
            "plantId", "P000000001",
            "jobId", "JOB01",
            "siteId", "ST001",
            "deptId", "DP001",
            "status", "DRAFT",
            "fileGroupId", ELEVEN_CHAR_FILE_GROUP_ID
        );

        mockMvc.perform(post("/api/workorders")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andExpect(status().isBadRequest());
    }

    private void postExpectCreated(String url, Map<String, Object> payload, String expectedFileGroupId) throws Exception {
        mockMvc.perform(post(url)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.fileGroupId").value(expectedFileGroupId));
    }
}
