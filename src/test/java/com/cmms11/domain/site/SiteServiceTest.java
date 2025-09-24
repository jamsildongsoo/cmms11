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
        SiteRequest request = new SiteRequest("S0001", "Main Site", MemberUserDetailsService.DEFAULT_COMPANY, "02-1234-5678", "서울시", "ACTIVE", "Primary");
        SiteResponse created = service.create(request);

        assertThat(created.siteId()).isEqualTo("S0001");
        assertThat(created.siteName()).isEqualTo("Main Site");

        Page<SiteResponse> page = service.list(null, PageRequest.of(0, 10));
        assertThat(page.getTotalElements()).isEqualTo(1);

        Page<SiteResponse> searchPage = service.list("Main", PageRequest.of(0, 10));
        assertThat(searchPage.getTotalElements()).isEqualTo(1);

        SiteRequest updateRequest = new SiteRequest("S0001", "Updated Site", MemberUserDetailsService.DEFAULT_COMPANY, "02-1234-5678", "서울시", "ACTIVE", "Updated");
        SiteResponse updated = service.update("S0001", updateRequest);
        assertThat(updated.siteName()).isEqualTo("Updated Site");

        service.delete("S0001");

        SiteId id = new SiteId(MemberUserDetailsService.DEFAULT_COMPANY, "S0001");
        Site deleted = repository.findById(id).orElseThrow();
        assertThat(deleted.getDeleteMark()).isEqualTo("Y");

        Page<SiteResponse> emptyPage = service.list(null, PageRequest.of(0, 10));
        assertThat(emptyPage.getTotalElements()).isZero();

        assertThatThrownBy(() -> service.get("S0001"))
            .isInstanceOf(NotFoundException.class);
    }
}
