package com.cmms11.inventory;

import static org.assertj.core.api.Assertions.assertThat;

import com.cmms11.common.seq.AutoNumberService;
import com.cmms11.common.upload.BulkUploadResult;
import com.cmms11.security.MemberUserDetailsService;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

@DataJpaTest
@Import({InventoryService.class, AutoNumberService.class})
class InventoryServiceUploadTest {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private InventoryRepository inventoryRepository;

    @BeforeEach
    void setUpAuthentication() {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(
                "uploader",
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
    void uploadCreatesInventories() throws Exception {
        String csv = String.join(
            "\n",
            "inventory_id,name,asset_id,dept_id,maker_name,spec,model,serial,file_group_id,note,status",
            ",베어링 6204,ASSET,D0001,SKF,내경 20mm,6204ZZ,,FG1001,예비품,ACTIVE",
            "I200000010,모터 윤활유,ASSET,D0002,SHELL,ISO VG 68,,LUB-2024,,창고 비축,ACTIVE"
        );
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "inventories.csv",
            "text/csv",
            csv.getBytes(StandardCharsets.UTF_8)
        );

        BulkUploadResult result = inventoryService.upload(file);

        assertThat(result.successCount()).isEqualTo(2);
        assertThat(result.failureCount()).isZero();

        List<Inventory> inventories = inventoryRepository.findAll();
        assertThat(inventories).hasSize(2);
        Inventory generated = inventories.stream()
            .filter(inv -> "베어링 6204".equals(inv.getName()))
            .findFirst()
            .orElseThrow();
        assertThat(generated.getId().getInventoryId()).startsWith("2");
        assertThat(generated.getCreatedBy()).isEqualTo("uploader");

        Inventory manual = inventoryRepository
            .findById(new InventoryId(MemberUserDetailsService.DEFAULT_COMPANY, "I200000010"))
            .orElseThrow();
        assertThat(manual.getName()).isEqualTo("모터 윤활유");
        assertThat(manual.getDeleteMark()).isEqualTo("N");
    }

    @Test
    void uploadCollectsErrorsAndPersistsValidRows() throws Exception {
        Inventory existing = new Inventory();
        existing.setId(new InventoryId(MemberUserDetailsService.DEFAULT_COMPANY, "I200000020"));
        existing.setName("기존 품목");
        existing.setDeleteMark("N");
        inventoryRepository.save(existing);

        String csv = String.join(
            "\n",
            "inventory_id,name,asset_id,dept_id,maker_name,spec,model,serial,file_group_id,note,status",
            "I200000020,중복 자재,ASSET,D0001,,,,,,ACTIVE",
            ",,ASSET,D0001,SKF,규격,MODEL-1,,,비고,ACTIVE",
            ",체결 볼트,ASSET,D0001,LS,규격 없음,BOLT-M10,,FG2001,비축 자재,INACTIVE"
        );
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "inventories-errors.csv",
            "text/csv",
            csv.getBytes(StandardCharsets.UTF_8)
        );

        BulkUploadResult result = inventoryService.upload(file);

        assertThat(result.successCount()).isEqualTo(1);
        assertThat(result.failureCount()).isEqualTo(2);
        assertThat(result.errors())
            .extracting("rowNumber")
            .containsExactlyInAnyOrder(2, 3);
        assertThat(result.errors())
            .extracting("message")
            .anyMatch(msg -> msg.contains("Inventory already exists"));
        assertThat(result.errors())
            .extracting("message")
            .anyMatch(msg -> msg.contains("필수 값이 비어 있습니다"));

        Inventory saved = inventoryRepository
            .findAll()
            .stream()
            .filter(inv -> "체결 볼트".equals(inv.getName()))
            .findFirst()
            .orElseThrow();
        assertThat(saved.getDeleteMark()).isEqualTo("N");
        assertThat(saved.getStatus()).isEqualTo("INACTIVE");
    }
}
