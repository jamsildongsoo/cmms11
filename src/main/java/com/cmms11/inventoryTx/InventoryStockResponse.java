package com.cmms11.inventoryTx;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 이름: InventoryStockResponse
 * 작성자: codex
 * 작성일: 2025-08-20
 * 수정일:
 * 프로그램 개요: 재고현황 응답 DTO.
 */
public record InventoryStockResponse(
    String storageId,
    String inventoryId,
    BigDecimal qty,
    BigDecimal amount,
    LocalDateTime updatedAt,
    String updatedBy
) {}
