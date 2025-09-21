package com.cmms11.inventoryTx;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "inventory_closing")
@Getter
@Setter
@NoArgsConstructor
public class InventoryClosing {

    @EmbeddedId
    private InventoryClosingId id;

    @Column(name = "begin_qty", precision = 18, scale = 3)
    private BigDecimal beginQty;

    @Column(name = "begin_amount", precision = 18, scale = 2)
    private BigDecimal beginAmount;

    @Column(name = "in_qty", precision = 18, scale = 3)
    private BigDecimal inQty;

    @Column(name = "in_amount", precision = 18, scale = 2)
    private BigDecimal inAmount;

    @Column(name = "out_qty", precision = 18, scale = 3)
    private BigDecimal outQty;

    @Column(name = "out_amount", precision = 18, scale = 2)
    private BigDecimal outAmount;

    @Column(name = "move_qty", precision = 18, scale = 2)
    private BigDecimal moveQty;

    @Column(name = "move_amount", precision = 18, scale = 2)
    private BigDecimal moveAmount;

    @Column(name = "adj_qty", precision = 18, scale = 3)
    private BigDecimal adjQty;

    @Column(name = "adj_amount", precision = 18, scale = 2)
    private BigDecimal adjAmount;

    @Column(name = "end_qty", precision = 18, scale = 3)
    private BigDecimal endQty;

    @Column(name = "end_amount", precision = 18, scale = 2)
    private BigDecimal endAmount;

    @Column(name = "status", length = 5)
    private String status;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "closed_by", length = 10)
    private String closedBy;

    public void setMoveQty(BigDecimal moveQty) {
        this.moveQty = moveQty != null ? moveQty.setScale(2, RoundingMode.HALF_UP) : null;
    }
}
