package com.cmms11.inventoryTx;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class InventoryClosingId implements Serializable {
    @Column(name = "company_id", length = 5, nullable = false)
    private String companyId;

    @Column(name = "yyyymm", length = 6, nullable = false)
    private String yyyymm;

    @Column(name = "storage_id", length = 5, nullable = false)
    private String storageId;

    @Column(name = "inventory_id", length = 10, nullable = false)
    private String inventoryId;
}

