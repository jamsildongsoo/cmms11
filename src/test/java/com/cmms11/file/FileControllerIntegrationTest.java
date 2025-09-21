package com.cmms11.file;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileSystemUtils;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@WithMockUser(username = "admin")
class FileControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Path storageRoot;

    @BeforeEach
    void setUp() throws Exception {
        storageRoot = Path.of("build", "test-uploads").toAbsolutePath();
        FileSystemUtils.deleteRecursively(storageRoot.toFile());
        Files.createDirectories(storageRoot);
    }

    @Test
    void uploadDownloadAndDeleteFiles() throws Exception {
        MockMultipartFile manual = new MockMultipartFile(
            "files",
            "manual.pdf",
            "application/pdf",
            "pdf-content".getBytes(StandardCharsets.UTF_8)
        );
        MockMultipartFile note = new MockMultipartFile(
            "files",
            "note.txt",
            MediaType.TEXT_PLAIN_VALUE,
            "hello world".getBytes(StandardCharsets.UTF_8)
        );

        MockMultipartHttpServletRequestBuilder uploadRequest = multipart("/api/files")
            .file(manual)
            .file(note)
            .param("refEntity", "PLANT")
            .param("refId", "TMP001")
            .with(csrf());

        MvcResult uploadResult = mockMvc.perform(uploadRequest)
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.items.length()").value(2))
            .andReturn();

        JsonNode uploadJson = objectMapper.readTree(uploadResult.getResponse().getContentAsString());
        String groupId = uploadJson.path("fileGroupId").asText();
        String manualFileId = uploadJson.path("items").get(0).path("fileId").asText();

        mockMvc.perform(get("/api/files").param("groupId", groupId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.fileGroupId").value(groupId))
            .andExpect(jsonPath("$.items.length()").value(2));

        mockMvc.perform(get("/api/files/{fileId}", manualFileId).param("groupId", groupId))
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("manual.pdf")))
            .andExpect(content().bytes(manual.getBytes()));

        mockMvc.perform(delete("/api/files/{fileId}", manualFileId).param("groupId", groupId).with(csrf()))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/files").param("groupId", groupId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items.length()").value(1));

        Path groupDir = storageRoot.resolve(groupId);
        assertThat(Files.exists(groupDir)).isTrue();
        try (Stream<Path> paths = Files.list(groupDir)) {
            long fileCount = paths.filter(Files::isRegularFile).count();
            assertThat(fileCount).isEqualTo(1);
        }
    }

    @Test
    void plantIntegrationPersistsFileGroup() throws Exception {
        MockMultipartFile attachment = new MockMultipartFile(
            "files",
            "specification.pdf",
            "application/pdf",
            "spec".getBytes(StandardCharsets.UTF_8)
        );

        MockMultipartHttpServletRequestBuilder uploadRequest = multipart("/api/files")
            .file(attachment)
            .with(csrf());

        MvcResult uploadResult = mockMvc.perform(uploadRequest)
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.items.length()").value(1))
            .andReturn();

        JsonNode uploadJson = objectMapper.readTree(uploadResult.getResponse().getContentAsString());
        String groupId = uploadJson.path("fileGroupId").asText();
        String plantPayload = """
            {
              \"name\": \"Compressor\",
              \"fileGroupId\": \"%s\"
            }
            """.formatted(groupId);

        MvcResult plantResult = mockMvc.perform(post("/api/plants")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(plantPayload))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.fileGroupId").value(groupId))
            .andReturn();

        JsonNode plantJson = objectMapper.readTree(plantResult.getResponse().getContentAsString());
        String plantId = plantJson.path("id").path("plantId").asText();
        assertThat(plantId).isNotBlank();

        mockMvc.perform(get("/api/plants/{plantId}", plantId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.fileGroupId").value(groupId));

        mockMvc.perform(get("/api/files").param("groupId", groupId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items.length()").value(1));
    }
}
