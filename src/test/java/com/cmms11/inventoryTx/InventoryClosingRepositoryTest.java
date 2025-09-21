package com.cmms11.inventoryTx;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class InventoryClosingRepositoryTest {

    @Autowired
    private InventoryClosingRepository inventoryClosingRepository;

    @Autowired
    private EntityManager entityManager;

    private InventoryClosingId sampleId;

    @BeforeEach
    void setUp() {
        sampleId = new InventoryClosingId("C0001", "202410", "ST001", "INV0000001");
    }

    @Test
    void findByIdShouldReturnPersistedEntity() {
        InventoryClosing closing = buildSampleClosing();
        inventoryClosingRepository.save(closing);
        entityManager.flush();
        entityManager.clear();

        Optional<InventoryClosing> found = inventoryClosingRepository.findById(sampleId);

        assertThat(found).isPresent();
        assertThat(found.get().getId().getYyyymm()).isEqualTo(sampleId.getYyyymm());
    }

    @Test
    void derivedQueryShouldUseYyyymmComponent() {
        InventoryClosing closing = buildSampleClosing();
        inventoryClosingRepository.save(closing);
        entityManager.flush();
        entityManager.clear();

        Optional<InventoryClosing> found = inventoryClosingRepository
            .findByIdCompanyIdAndIdYyyymmAndIdStorageIdAndIdInventoryId(
                sampleId.getCompanyId(),
                sampleId.getYyyymm(),
                sampleId.getStorageId(),
                sampleId.getInventoryId()
            );

        assertThat(found).isPresent();
        assertThat(found.get().getId().getYyyymm()).isEqualTo(sampleId.getYyyymm());
    }

    private InventoryClosing buildSampleClosing() {
        InventoryClosing closing = new InventoryClosing();
        closing.setId(sampleId);
        closing.setBeginQty(new BigDecimal("10.000"));
        closing.setBeginAmount(new BigDecimal("1000.00"));
        closing.setInQty(new BigDecimal("5.000"));
        closing.setInAmount(new BigDecimal("500.00"));
        closing.setOutQty(new BigDecimal("3.000"));
        closing.setOutAmount(new BigDecimal("300.00"));
        closing.setMoveQty(new BigDecimal("1.000"));
        closing.setMoveAmount(new BigDecimal("100.00"));
        closing.setAdjQty(new BigDecimal("0.000"));
        closing.setAdjAmount(new BigDecimal("0.00"));
        closing.setEndQty(new BigDecimal("12.000"));
        closing.setEndAmount(new BigDecimal("1200.00"));
        closing.setStatus("CLSD");
        closing.setClosedAt(LocalDateTime.of(2024, 10, 31, 23, 59));
        closing.setClosedBy("tester");
        return closing;
    }
}
