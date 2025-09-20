package com.cmms11.inventoryTx;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "inventory_history")
@Getter
@Setter
@NoArgsConstructor
public class InventoryHistory {

    @EmbeddedId
    private InventoryHistoryId id;

    @Column(name = "inventory_id", length = 10)
    private String inventoryId;

    @Column(name = "storage_id", length = 5)
    private String storageId;

    @Column(name = "tx_type", length = 5)
    private String txType;

    @Column(name = "ref_no", length = 10)
    private String refNo;

    @Column(name = "ref_line")
    private Integer refLine;

    @Column(name = "tx_date")
    private LocalDate txDate;

    @Column(name = "in_qty", precision = 18, scale = 3)
    private BigDecimal inQty;

    @Column(name = "out_qty", precision = 18, scale = 3)
    private BigDecimal outQty;

    @Column(name = "unit_cost", precision = 18, scale = 6)
    private BigDecimal unitCost;

    @Column(name = "amount", precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(name = "note", length = 500)
    private String note;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 10)
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 10)
    private String updatedBy;
}

