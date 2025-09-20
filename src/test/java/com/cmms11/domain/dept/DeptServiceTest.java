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
        DeptCreateRequest request = new DeptCreateRequest("D0001", "Maintenance", "Handles maintenance", null);
        Dept created = service.create(request, "tester");

        assertThat(created.getId().getCompanyId()).isEqualTo(MemberUserDetailsService.DEFAULT_COMPANY);
        assertThat(created.getDeleteMark()).isEqualTo("N");
        assertThat(created.getCreatedBy()).isEqualTo("tester");

        Page<Dept> page = service.list(null, PageRequest.of(0, 10));
        assertThat(page.getTotalElements()).isEqualTo(1);

        Page<Dept> search = service.list("Maint", PageRequest.of(0, 10));
        assertThat(search.getTotalElements()).isEqualTo(1);

        Dept updated = service.update("D0001",
            new DeptUpdateRequest("Maintenance Updated", "Updated note", "P0001"),
            "updater");

        assertThat(updated.getName()).isEqualTo("Maintenance Updated");
        assertThat(updated.getParentId()).isEqualTo("P0001");
        assertThat(updated.getUpdatedBy()).isEqualTo("updater");

        service.delete("D0001", "deleter");

        DeptId id = new DeptId(MemberUserDetailsService.DEFAULT_COMPANY, "D0001");
        Dept deleted = repository.findById(id).orElseThrow();
        assertThat(deleted.getDeleteMark()).isEqualTo("Y");
        assertThat(deleted.getUpdatedBy()).isEqualTo("deleter");

        Page<Dept> empty = service.list(null, PageRequest.of(0, 10));
        assertThat(empty.getTotalElements()).isZero();

        assertThatThrownBy(() -> service.get("D0001"))
            .isInstanceOf(NotFoundException.class);
    }
}
