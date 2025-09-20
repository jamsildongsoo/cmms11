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

import com.cmms11.domain.dept.Dept;
import com.cmms11.domain.dept.DeptCreateRequest;
import com.cmms11.domain.dept.DeptId;
import com.cmms11.domain.dept.DeptService;
import com.cmms11.domain.dept.DeptUpdateRequest;
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

@WebMvcTest(DeptController.class)
@AutoConfigureMockMvc(addFilters = false)
class DeptControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DeptService service;

    @Test
    void listDeptsDelegatesToService() throws Exception {
        Dept dept = new Dept();
        dept.setId(new DeptId("C0001", "D0001"));
        dept.setName("Maintenance");
        dept.setDeleteMark("N");
        Page<Dept> page = new PageImpl<>(List.of(dept), PageRequest.of(0, 20), 1);

        when(service.list(eq("Main"), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/domain/depts").param("q", "Main"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].deptId").value("D0001"))
            .andExpect(jsonPath("$.content[0].name").value("Maintenance"));

        verify(service).list(eq("Main"), any(Pageable.class));
    }

    @Test
    void getDeptReturnsEntity() throws Exception {
        Dept dept = new Dept();
        dept.setId(new DeptId("C0001", "D0001"));
        dept.setName("Maintenance");
        dept.setDeleteMark("N");

        when(service.get("D0001")).thenReturn(dept);

        mockMvc.perform(get("/api/domain/depts/D0001"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.deptId").value("D0001"))
            .andExpect(jsonPath("$.name").value("Maintenance"));

        verify(service).get("D0001");
    }

    @Test
    void createDeptReturnsCreated() throws Exception {
        Dept dept = new Dept();
        dept.setId(new DeptId("C0001", "D0001"));
        dept.setName("Maintenance");
        dept.setDeleteMark("N");

        when(service.create(any(DeptCreateRequest.class), isNull())).thenReturn(dept);

        mockMvc.perform(post("/api/domain/depts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"deptId\":\"D0001\",\"name\":\"Maintenance\",\"note\":\"Handles\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.deptId").value("D0001"))
            .andExpect(jsonPath("$.name").value("Maintenance"));

        verify(service).create(any(DeptCreateRequest.class), isNull());
    }

    @Test
    void updateDeptReturnsUpdated() throws Exception {
        Dept dept = new Dept();
        dept.setId(new DeptId("C0001", "D0001"));
        dept.setName("Updated Dept");
        dept.setDeleteMark("N");

        when(service.update(eq("D0001"), any(DeptUpdateRequest.class), isNull())).thenReturn(dept);

        mockMvc.perform(put("/api/domain/depts/D0001")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Updated Dept\",\"note\":\"Updated\",\"parentId\":\"P0001\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.deptId").value("D0001"))
            .andExpect(jsonPath("$.name").value("Updated Dept"));

        verify(service).update(eq("D0001"), any(DeptUpdateRequest.class), isNull());
    }

    @Test
    void deleteDeptReturnsNoContent() throws Exception {
        doNothing().when(service).delete(anyString(), any());

        mockMvc.perform(delete("/api/domain/depts/D0001"))
            .andExpect(status().isNoContent());

        verify(service).delete(eq("D0001"), isNull());
    }
}
