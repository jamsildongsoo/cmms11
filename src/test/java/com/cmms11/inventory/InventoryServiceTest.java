package com.cmms11.inventory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.cmms11.common.error.NotFoundException;
import com.cmms11.common.seq.AutoNumberService;
import com.cmms11.security.MemberUserDetailsService;
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
 * 이름: InventoryServiceTest
 * 작성자: codex
 * 작성일: 2025-08-20
 * 수정일:
 * 프로그램 개요: InventoryService의 CRUD 동작을 검증하는 단위 테스트.
 */
@DataJpaTest
@Import({InventoryService.class, AutoNumberService.class})
class InventoryServiceTest {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private InventoryRepository inventoryRepository;

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
    void createWithoutIdGeneratesNewInventoryNumber() {
        InventoryRequest request = new InventoryRequest(
            null,
            "베어링",
            "ASSET",
            "D0001",
            "SKF",
            "6205ZZ",
            "MODEL-01",
            "SN-001",
            "FG0001",
            "예비 부품",
            "ACTIVE"
        );

        InventoryResponse response = inventoryService.create(request);

        assertThat(response.inventoryId()).startsWith("2");
        assertThat(response.inventoryId()).hasSize(10);
        assertThat(response.deleteMark()).isEqualTo("N");
        assertThat(response.createdBy()).isEqualTo("tester");

        Inventory saved = inventoryRepository
            .findById(new InventoryId(MemberUserDetailsService.DEFAULT_COMPANY, response.inventoryId()))
            .orElseThrow();
        assertThat(saved.getName()).isEqualTo("베어링");
        assertThat(saved.getDeleteMark()).isEqualTo("N");
    }

    @Test
    void createReactivatesSoftDeletedInventory() {
        Inventory existing = new Inventory();
        existing.setId(new InventoryId(MemberUserDetailsService.DEFAULT_COMPANY, "2000000001"));
        existing.setName("사용중지 품목");
        existing.setDeleteMark("Y");
        inventoryRepository.save(existing);

        InventoryRequest request = new InventoryRequest(
            "2000000001",
            "재사용 품목",
            "ASSET",
            "D0002",
            null,
            null,
            null,
            null,
            null,
            "재활성화",
            "ACTIVE"
        );

        InventoryResponse response = inventoryService.create(request);

        assertThat(response.inventoryId()).isEqualTo("2000000001");
        assertThat(response.deleteMark()).isEqualTo("N");

        Inventory saved = inventoryRepository
            .findById(new InventoryId(MemberUserDetailsService.DEFAULT_COMPANY, "2000000001"))
            .orElseThrow();
        assertThat(saved.getDeleteMark()).isEqualTo("N");
        assertThat(saved.getDeptId()).isEqualTo("D0002");
    }

    @Test
    void createThrowsWhenInventoryAlreadyExists() {
        Inventory existing = new Inventory();
        existing.setId(new InventoryId(MemberUserDetailsService.DEFAULT_COMPANY, "2000000002"));
        existing.setName("활성 품목");
        existing.setDeleteMark("N");
        inventoryRepository.save(existing);

        InventoryRequest request = new InventoryRequest(
            "2000000002",
            "중복 품목",
            "ASSET",
            "D0001",
            null,
            null,
            null,
            null,
            null,
            null,
            "ACTIVE"
        );

        assertThatThrownBy(() -> inventoryService.create(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("2000000002");
    }

    @Test
    void updateModifiesExistingInventory() {
        InventoryResponse created = inventoryService.create(
            new InventoryRequest(
                null,
                "모터",
                "ASSET",
                "D0001",
                "HYUNDAI",
                "5HP",
                "M-100",
                "SN-XYZ",
                null,
                null,
                "ACTIVE"
            )
        );

        InventoryRequest updateRequest = new InventoryRequest(
            created.inventoryId(),
            "모터-수정",
            "ASSET",
            "D0003",
            "HYUNDAI",
            "7HP",
            "M-100",
            "SN-XYZ",
            "FG-123",
            "전압 변경",
            "INACTIVE"
        );

        InventoryResponse updated = inventoryService.update(created.inventoryId(), updateRequest);

        assertThat(updated.name()).isEqualTo("모터-수정");
        assertThat(updated.deptId()).isEqualTo("D0003");
        assertThat(updated.status()).isEqualTo("INACTIVE");
    }

    @Test
    void deleteMarksInventoryAsDeleted() {
        InventoryResponse created = inventoryService.create(
            new InventoryRequest(
                null,
                "펌프",
                "ASSET",
                "D0001",
                null,
                null,
                null,
                null,
                null,
                null,
                "ACTIVE"
            )
        );

        inventoryService.delete(created.inventoryId());

        Inventory saved = inventoryRepository
            .findById(new InventoryId(MemberUserDetailsService.DEFAULT_COMPANY, created.inventoryId()))
            .orElseThrow();
        assertThat(saved.getDeleteMark()).isEqualTo("Y");
    }

    @Test
    void getThrowsWhenInventoryMissing() {
        assertThatThrownBy(() -> inventoryService.get("2999999999"))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    void listReturnsActiveInventoriesMatchingKeyword() {
        inventoryService.create(
            new InventoryRequest(
                null,
                "볼트 M10",
                "ASSET",
                "D0001",
                null,
                null,
                null,
                null,
                null,
                null,
                "ACTIVE"
            )
        );
        inventoryService.create(
            new InventoryRequest(
                null,
                "볼트 M12",
                "ASSET",
                "D0001",
                null,
                null,
                null,
                null,
                null,
                null,
                "ACTIVE"
            )
        );
        InventoryResponse deleted = inventoryService.create(
            new InventoryRequest(
                null,
                "와셔",
                "ASSET",
                "D0001",
                null,
                null,
                null,
                null,
                null,
                null,
                "ACTIVE"
            )
        );
        inventoryService.delete(deleted.inventoryId());

        Page<InventoryResponse> page = inventoryService.list("볼트", PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).allMatch(item -> item.name().contains("볼트"));
    }
}
