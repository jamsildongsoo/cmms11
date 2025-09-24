package com.cmms11.domain.dept;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.cmms11.common.error.NotFoundException;
import com.cmms11.security.MemberUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@DataJpaTest
@Import(DeptService.class)
class DeptServiceTest {

    @Autowired
    private DeptService service;

    @Autowired
    private DeptRepository repository;

    @Test
    void createUpdateAndSoftDeleteDept() {
        DeptRequest request = new DeptRequest("D0001", "Maintenance", "02-1234-5678", "서울시", "ACTIVE", null, "Handles maintenance");
        DeptResponse created = service.create(request);

        assertThat(created.deptId()).isEqualTo("D0001");
        assertThat(created.deptName()).isEqualTo("Maintenance");

        Page<DeptResponse> page = service.list(null, PageRequest.of(0, 10));
        assertThat(page.getTotalElements()).isEqualTo(1);

        Page<DeptResponse> search = service.list("Maint", PageRequest.of(0, 10));
        assertThat(search.getTotalElements()).isEqualTo(1);

        DeptRequest updateRequest = new DeptRequest("D0001", "Maintenance Updated", "02-1234-5678", "서울시", "ACTIVE", null, "Updated note");
        DeptResponse updated = service.update("D0001", updateRequest);

        assertThat(updated.deptName()).isEqualTo("Maintenance Updated");

        service.delete("D0001");

        DeptId id = new DeptId(MemberUserDetailsService.DEFAULT_COMPANY, "D0001");
        Dept deleted = repository.findById(id).orElseThrow();
        assertThat(deleted.getDeleteMark()).isEqualTo("Y");

        Page<DeptResponse> empty = service.list(null, PageRequest.of(0, 10));
        assertThat(empty.getTotalElements()).isZero();

        assertThatThrownBy(() -> service.get("D0001"))
            .isInstanceOf(NotFoundException.class);
    }
}
