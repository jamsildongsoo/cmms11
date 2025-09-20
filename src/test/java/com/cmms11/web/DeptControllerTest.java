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
import com.cmms11.domain.dept.DeptId;
import com.cmms11.domain.dept.DeptRequest;
import com.cmms11.domain.dept.DeptResponse;
import com.cmms11.domain.dept.DeptService;
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
        DeptResponse deptResponse = new DeptResponse("D0001", "Maintenance", "Handles maintenance", "N", null, null, null, null);
        Page<DeptResponse> page = new PageImpl<>(List.of(deptResponse), PageRequest.of(0, 20), 1);

        when(service.list(eq("Main"), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/domain/depts").param("q", "Main"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].deptId").value("D0001"))
            .andExpect(jsonPath("$.content[0].name").value("Maintenance"));

        verify(service).list(eq("Main"), any(Pageable.class));
    }

    @Test
    void getDeptReturnsEntity() throws Exception {
        DeptResponse deptResponse = new DeptResponse("D0001", "Maintenance", "Handles maintenance", "N", null, null, null, null);

        when(service.get("D0001")).thenReturn(deptResponse);

        mockMvc.perform(get("/api/domain/depts/D0001"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.deptId").value("D0001"))
            .andExpect(jsonPath("$.name").value("Maintenance"));

        verify(service).get("D0001");
    }

    @Test
    void createDeptReturnsCreated() throws Exception {
        DeptResponse deptResponse = new DeptResponse("D0001", "Maintenance", "Handles maintenance", "N", null, null, null, null);

        when(service.create(any(DeptRequest.class))).thenReturn(deptResponse);

        mockMvc.perform(post("/api/domain/depts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"deptId\":\"D0001\",\"name\":\"Maintenance\",\"note\":\"Handles\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.deptId").value("D0001"))
            .andExpect(jsonPath("$.name").value("Maintenance"));

        verify(service).create(any(DeptRequest.class));
    }

    @Test
    void updateDeptReturnsUpdated() throws Exception {
        DeptResponse deptResponse = new DeptResponse("D0001", "Updated Dept", "Updated", "N", null, null, null, null);

        when(service.update(eq("D0001"), any(DeptRequest.class))).thenReturn(deptResponse);

        mockMvc.perform(put("/api/domain/depts/D0001")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"deptId\":\"D0001\",\"name\":\"Updated Dept\",\"note\":\"Updated\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.deptId").value("D0001"))
            .andExpect(jsonPath("$.name").value("Updated Dept"));

        verify(service).update(eq("D0001"), any(DeptRequest.class));
    }

    @Test
    void deleteDeptReturnsNoContent() throws Exception {
        doNothing().when(service).delete(anyString());

        mockMvc.perform(delete("/api/domain/depts/D0001"))
            .andExpect(status().isNoContent());

        verify(service).delete(eq("D0001"));
    }
}
