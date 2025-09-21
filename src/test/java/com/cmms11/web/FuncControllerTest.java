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

import com.cmms11.domain.func.Func;
import com.cmms11.domain.func.FuncId;
import com.cmms11.domain.func.FuncRepository;
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
class FuncControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FuncRepository repository;

    @Test
    void listFunctionsSupportsPaginationAndSearch() throws Exception {
        saveFunc("F0001", "공정관리");
        saveFunc("F0002", "유틸리티");

        mockMvc.perform(get("/api/domain/funcs").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()", is(2)));

        mockMvc.perform(get("/api/domain/funcs")
                        .param("q", "유틸")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()", is(1)))
                .andExpect(jsonPath("$.content[0].name").value("유틸리티"));
    }

    @Test
    void createUpdateAndDeleteFunc() throws Exception {
        String createPayload = """
            {
              \"funcId\": \"F0100\",
              \"name\": \"안전관리\",
              \"note\": \"신규 기능\"
            }
            """;

        mockMvc.perform(post("/api/domain/funcs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPayload)
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.funcId").value("F0100"))
                .andExpect(jsonPath("$.deleteMark").value("N"));

        FuncId id = new FuncId("C0001", "F0100");
        Func saved = repository.findById(id).orElseThrow();
        assertThat(saved.getDeleteMark()).isEqualTo("N");

        String updatePayload = """
            {
              \"funcId\": \"F0100\",
              \"name\": \"안전관리\",
              \"note\": \"점검 주관 부서\"
            }
            """;

        mockMvc.perform(put("/api/domain/funcs/{funcId}", "F0100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.note").value("점검 주관 부서"));

        mockMvc.perform(delete("/api/domain/funcs/{funcId}", "F0100")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        Func deleted = repository.findById(id).orElseThrow();
        assertThat(deleted.getDeleteMark()).isEqualTo("Y");
    }

    @Test
    void funcTemplatesAreServedThroughLayout() throws Exception {
        mockMvc.perform(get("/domain/func/list.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("card-title")));

        mockMvc.perform(get("/domain/func/form.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("card-title")));
    }

    private void saveFunc(String funcId, String name) {
        Func func = new Func();
        func.setId(new FuncId("C0001", funcId));
        func.setName(name);
        func.setDeleteMark("N");
        func.setCreatedAt(LocalDateTime.now());
        func.setUpdatedAt(LocalDateTime.now());
        repository.save(func);
    }
}
