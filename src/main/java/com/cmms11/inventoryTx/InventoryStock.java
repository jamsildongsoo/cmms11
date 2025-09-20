package com.cmms11.inventoryTx;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "inventory_stock")
@Getter
@Setter
@NoArgsConstructor
public class InventoryStock {

    @EmbeddedId
    private InventoryStockId id;

    @Column(name = "qty", precision = 18, scale = 3)
    private BigDecimal qty;

    @Column(name = "amount", precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 10)
    private String updatedBy;
}

