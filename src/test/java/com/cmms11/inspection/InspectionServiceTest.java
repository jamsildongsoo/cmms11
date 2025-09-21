package com.cmms11.inspection;

import static org.assertj.core.api.Assertions.assertThat;

import com.cmms11.common.seq.AutoNumberService;
import com.cmms11.security.MemberUserDetailsService;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

@DataJpaTest
@Import({InspectionService.class, AutoNumberService.class})
class InspectionServiceTest {

    @Autowired
    private InspectionService inspectionService;

    @Autowired
    private InspectionRepository inspectionRepository;

    @Autowired
    private InspectionItemRepository inspectionItemRepository;

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
    void createInspectionPersistsItems() {
        InspectionRequest request = defaultRequest(List.of(sampleItemRequest("압력", "bar", "1.60")));

        InspectionResponse response = inspectionService.create(request);

        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).unit()).isEqualTo("bar");
        assertThat(response.items().get(0).resultVal()).isEqualTo("1.60");

        List<InspectionItem> savedItems = inspectionItemRepository
            .findByIdCompanyIdAndIdInspectionIdOrderByIdLineNo(
                MemberUserDetailsService.DEFAULT_COMPANY,
                response.inspectionId()
            );
        assertThat(savedItems).hasSize(1);
        assertThat(savedItems.get(0).getUnit()).isEqualTo("bar");
        assertThat(savedItems.get(0).getResultVal()).isEqualTo("1.60");
        assertThat(savedItems.get(0).getId().getLineNo()).isEqualTo(1);
    }

    @Test
    void updateInspectionReplacesItems() {
        InspectionResponse created = inspectionService.create(
            defaultRequest(List.of(sampleItemRequest("압력", "bar", "1.60")))
        );

        InspectionRequest updateRequest = defaultRequest(
            List.of(
                sampleItemRequest("온도", "℃", "35"),
                sampleItemRequest("소음", "dB", "70")
            )
        );

        InspectionResponse updated = inspectionService.update(created.inspectionId(), updateRequest);

        assertThat(updated.items()).hasSize(2);
        assertThat(updated.items().get(0).name()).isEqualTo("온도");
        assertThat(updated.items().get(1).name()).isEqualTo("소음");

        List<InspectionItem> savedItems = inspectionItemRepository
            .findByIdCompanyIdAndIdInspectionIdOrderByIdLineNo(
                MemberUserDetailsService.DEFAULT_COMPANY,
                created.inspectionId()
            );
        assertThat(savedItems).hasSize(2);
        assertThat(savedItems.get(0).getId().getLineNo()).isEqualTo(1);
        assertThat(savedItems.get(1).getId().getLineNo()).isEqualTo(2);
    }

    @Test
    void deleteInspectionRemovesItems() {
        InspectionResponse created = inspectionService.create(
            defaultRequest(List.of(sampleItemRequest("압력", "bar", "1.60")))
        );

        inspectionService.delete(created.inspectionId());

        assertThat(
            inspectionRepository.findByIdCompanyIdAndIdInspectionId(
                MemberUserDetailsService.DEFAULT_COMPANY,
                created.inspectionId()
            )
        )
            .isEmpty();

        List<InspectionItem> remainingItems = inspectionItemRepository
            .findByIdCompanyIdAndIdInspectionIdOrderByIdLineNo(
                MemberUserDetailsService.DEFAULT_COMPANY,
                created.inspectionId()
            );
        assertThat(remainingItems).isEmpty();
    }

    @Test
    void getInspectionIncludesItems() {
        InspectionResponse created = inspectionService.create(
            defaultRequest(List.of(sampleItemRequest("압력", "bar", "1.60")))
        );

        InspectionResponse fetched = inspectionService.get(created.inspectionId());

        assertThat(fetched.items()).hasSize(1);
        assertThat(fetched.items().get(0).name()).isEqualTo("압력");
    }

    private InspectionRequest defaultRequest(List<InspectionItemRequest> items) {
        return new InspectionRequest(
            null,
            "예방점검",
            "P0001",
            "J0001",
            "S0001",
            "D0001",
            "M0001",
            LocalDate.of(2025, 8, 20),
            LocalDate.of(2025, 8, 21),
            "READY",
            "FG0001",
            "비고",
            items
        );
    }

    private InspectionItemRequest sampleItemRequest(String name, String unit, String resultVal) {
        return new InspectionItemRequest(name, "방법", "1", "2", "1.5", unit, resultVal, "비고");
    }
}
