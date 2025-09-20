package com.cmms11.domain.company;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.cmms11.common.error.NotFoundException;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 이름: CompanyServiceTest
 * 작성자: codex
 * 작성일: 2025-08-20
 * 수정일:
 * 프로그램 개요: CompanyService의 주요 CRUD 동작을 검증하는 단위 테스트.
 */
@DataJpaTest
@Import(CompanyService.class)
class CompanyServiceTest {

    @Autowired
    private CompanyService service;

    @Autowired
    private CompanyRepository repository;

    @BeforeEach
    void setUpAuthentication() {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(
                "tester",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
            )
        );
    }

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createNewCompany_insertsRecordWithAuditFields() {
        CompanyRequest request = new CompanyRequest("C9000", "Test Company", "memo");

        CompanyResponse response = service.create(request);

        assertThat(response.companyId()).isEqualTo("C9000");
        assertThat(response.deleteMark()).isEqualTo("N");
        assertThat(response.createdBy()).isEqualTo("tester");

        Company saved = repository.findById("C9000").orElseThrow();
        assertThat(saved.getName()).isEqualTo("Test Company");
        assertThat(saved.getDeleteMark()).isEqualTo("N");
    }

    @Test
    void createReactivatesSoftDeletedCompany() {
        Company existing = new Company();
        existing.setCompanyId("C8000");
        existing.setName("Legacy");
        existing.setDeleteMark("Y");
        repository.save(existing);

        CompanyResponse response = service.create(new CompanyRequest("C8000", "Reactivated", "note"));

        assertThat(response.companyId()).isEqualTo("C8000");
        assertThat(response.deleteMark()).isEqualTo("N");
        assertThat(repository.findById("C8000").orElseThrow().getDeleteMark()).isEqualTo("N");
    }

    @Test
    void createThrowsWhenCompanyAlreadyExists() {
        Company existing = new Company();
        existing.setCompanyId("C7000");
        existing.setName("Active");
        existing.setDeleteMark("N");
        repository.save(existing);

        assertThatThrownBy(() -> service.create(new CompanyRequest("C7000", "Another", null)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("C7000");
    }

    @Test
    void updateModifiesExistingCompany() {
        Company existing = new Company();
        existing.setCompanyId("C6000");
        existing.setName("Before");
        existing.setDeleteMark("N");
        repository.save(existing);

        CompanyResponse response = service.update("C6000", new CompanyRequest("C6000", "After", "memo"));

        assertThat(response.name()).isEqualTo("After");
        assertThat(repository.findById("C6000").orElseThrow().getName()).isEqualTo("After");
    }

    @Test
    void deleteMarksCompanyAsDeleted() {
        Company existing = new Company();
        existing.setCompanyId("C5000");
        existing.setName("ToRemove");
        existing.setDeleteMark("N");
        repository.save(existing);

        service.delete("C5000");

        assertThat(repository.findById("C5000").orElseThrow().getDeleteMark()).isEqualTo("Y");
    }

    @Test
    void getThrowsNotFoundForMissingCompany() {
        assertThatThrownBy(() -> service.get("XXXX"))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    void listReturnsOnlyActiveCompaniesMatchingKeyword() {
        Company alpha = new Company();
        alpha.setCompanyId("C1000");
        alpha.setName("Alpha Plant");
        repository.save(alpha);

        Company beta = new Company();
        beta.setCompanyId("C1001");
        beta.setName("Beta Plant");
        repository.save(beta);

        Company deleted = new Company();
        deleted.setCompanyId("C1002");
        deleted.setName("Alpha Archived");
        deleted.setDeleteMark("Y");
        repository.save(deleted);

        Page<CompanyResponse> result = service.list("Alpha", PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).companyId()).isEqualTo("C1000");
    }
}
