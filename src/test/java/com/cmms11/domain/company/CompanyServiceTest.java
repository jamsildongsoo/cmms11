package com.cmms11.domain.company;

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
@Import(CompanyService.class)
class CompanyServiceTest {

    @Autowired
    private CompanyService service;

    @Autowired
    private CompanyRepository repository;

    @Test
    void createUpdateAndSoftDeleteCompany() {
        CompanyRequest request = new CompanyRequest("Acme Corp", "First company");
        Company created = service.create(request, "tester");

        assertThat(created.getCompanyId()).isEqualTo(MemberUserDetailsService.DEFAULT_COMPANY);
        assertThat(created.getDeleteMark()).isEqualTo("N");
        assertThat(created.getCreatedAt()).isNotNull();
        assertThat(created.getCreatedBy()).isEqualTo("tester");

        Page<Company> page = service.list(null, PageRequest.of(0, 10));
        assertThat(page.getTotalElements()).isEqualTo(1);

        Company fetched = service.get(MemberUserDetailsService.DEFAULT_COMPANY);
        assertThat(fetched.getName()).isEqualTo("Acme Corp");

        Company updated = service.update(MemberUserDetailsService.DEFAULT_COMPANY,
            new CompanyRequest("Acme Updated", "Updated"),
            "updater");

        assertThat(updated.getName()).isEqualTo("Acme Updated");
        assertThat(updated.getUpdatedBy()).isEqualTo("updater");
        assertThat(updated.getUpdatedAt()).isNotNull();

        service.delete(MemberUserDetailsService.DEFAULT_COMPANY, "deleter");

        Company deleted = repository.findById(MemberUserDetailsService.DEFAULT_COMPANY).orElseThrow();
        assertThat(deleted.getDeleteMark()).isEqualTo("Y");
        assertThat(deleted.getUpdatedBy()).isEqualTo("deleter");

        Page<Company> emptyPage = service.list(null, PageRequest.of(0, 10));
        assertThat(emptyPage.getTotalElements()).isZero();

        assertThatThrownBy(() -> service.get(MemberUserDetailsService.DEFAULT_COMPANY))
            .isInstanceOf(NotFoundException.class);
    }
}
