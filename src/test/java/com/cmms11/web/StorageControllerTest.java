package com.cmms11.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cmms11.domain.storage.Storage;
import com.cmms11.domain.storage.StorageId;
import com.cmms11.domain.storage.StorageRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@WithMockUser(username = "admin")
class StorageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StorageRepository repository;

    @Test
    void listStoragesSupportsPaginationAndSearch() throws Exception {
        saveStorage("ST001", "메인창고");
        saveStorage("ST002", "부자재창고");

        mockMvc.perform(get("/api/domain/storages").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()", is(2)));

        mockMvc.perform(get("/api/domain/storages")
                        .param("q", "부자재")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()", is(1)))
                .andExpect(jsonPath("$.content[0].name").value("부자재창고"));
    }

    @Test
    void createUpdateAndDeleteStorage() throws Exception {
        String createPayload = """
            {
              \"storageId\": \"ST100\",
              \"name\": \"예비창고\",
              \"note\": \"임시 보관\"
            }
            """;

        mockMvc.perform(post("/api/domain/storages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPayload)
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.storageId").value("ST100"))
                .andExpect(jsonPath("$.deleteMark").value("N"));

        StorageId id = new StorageId("C0001", "ST100");
        Storage saved = repository.findById(id).orElseThrow();
        assertThat(saved.getDeleteMark()).isEqualTo("N");

        String updatePayload = """
            {
              \"storageId\": \"ST100\",
              \"name\": \"예비창고\",
              \"note\": \"재고 초과분 보관\"
            }
            """;

        mockMvc.perform(put("/api/domain/storages/{storageId}", "ST100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.note").value("재고 초과분 보관"));

        mockMvc.perform(delete("/api/domain/storages/{storageId}", "ST100")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        Storage deleted = repository.findById(id).orElseThrow();
        assertThat(deleted.getDeleteMark()).isEqualTo("Y");
    }

    @Test
    void storageTemplatesAreServedThroughLayout() throws Exception {
        mockMvc.perform(get("/domain/storage/list.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("card-title")));

        mockMvc.perform(get("/domain/storage/form.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("card-title")));
    }

    private void saveStorage(String storageId, String name) {
        Storage storage = new Storage();
        storage.setId(new StorageId("C0001", storageId));
        storage.setName(name);
        storage.setDeleteMark("N");
        storage.setCreatedAt(LocalDateTime.now());
        storage.setUpdatedAt(LocalDateTime.now());
        repository.save(storage);
    }
}
