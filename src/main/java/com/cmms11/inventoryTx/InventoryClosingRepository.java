package com.cmms11.inventoryTx;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Inventory closing 집계 내역을 조회/저장하는 JPA 레포지토리.
 */
public interface InventoryClosingRepository extends JpaRepository<InventoryClosing, InventoryClosingId> {

    Optional<InventoryClosing> findByIdCompanyIdAndIdYyyymmAndIdStorageIdAndIdInventoryId(
        String companyId,
        String yyyymm,
        String storageId,
        String inventoryId
    );
}
