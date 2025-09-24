package com.cmms11.inventoryTx;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 이름: InventoryTxRequest
 * 작성자: codex
 * 작성일: 2025-08-20
 * 수정일:
 * 프로그램 개요: 재고거래 요청 DTO.
 */
public record InventoryTxRequest(
    String inventoryId,
    String storageId,
    String txType,
    String refNo,
    Integer refLine,
    LocalDate txDate,
    BigDecimal inQty,
    BigDecimal outQty,
    BigDecimal unitCost,
    BigDecimal amount,
    String note
) {}
