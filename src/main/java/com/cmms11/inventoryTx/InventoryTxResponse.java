package com.cmms11.inventoryTx;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 이름: InventoryTxResponse
 * 작성자: codex
 * 작성일: 2025-08-20
 * 수정일:
 * 프로그램 개요: 재고거래 응답 DTO.
 */
public record InventoryTxResponse(
    String historyId,
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
    String note,
    LocalDateTime createdAt,
    String createdBy,
    LocalDateTime updatedAt,
    String updatedBy
) {}
