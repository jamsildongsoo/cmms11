package com.cmms11.domain.site;

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
@Import(SiteService.class)
class SiteServiceTest {

    @Autowired
    private SiteService service;

    @Autowired
    private SiteRepository repository;

    @Test
    void createSearchAndSoftDeleteSite() {
        SiteCreateRequest request = new SiteCreateRequest("S0001", "Main Site", "Primary");
        Site created = service.create(request, "tester");

        assertThat(created.getId().getCompanyId()).isEqualTo(MemberUserDetailsService.DEFAULT_COMPANY);
        assertThat(created.getDeleteMark()).isEqualTo("N");
        assertThat(created.getCreatedBy()).isEqualTo("tester");

        Page<Site> page = service.list(null, PageRequest.of(0, 10));
        assertThat(page.getTotalElements()).isEqualTo(1);

        Page<Site> searchPage = service.list("Main", PageRequest.of(0, 10));
        assertThat(searchPage.getTotalElements()).isEqualTo(1);

        Site updated = service.update("S0001", new SiteUpdateRequest("Updated Site", "Updated"), "updater");
        assertThat(updated.getName()).isEqualTo("Updated Site");
        assertThat(updated.getUpdatedBy()).isEqualTo("updater");

        service.delete("S0001", "deleter");

        SiteId id = new SiteId(MemberUserDetailsService.DEFAULT_COMPANY, "S0001");
        Site deleted = repository.findById(id).orElseThrow();
        assertThat(deleted.getDeleteMark()).isEqualTo("Y");
        assertThat(deleted.getUpdatedBy()).isEqualTo("deleter");

        Page<Site> emptyPage = service.list(null, PageRequest.of(0, 10));
        assertThat(emptyPage.getTotalElements()).isZero();

        assertThatThrownBy(() -> service.get("S0001"))
            .isInstanceOf(NotFoundException.class);
    }
}
