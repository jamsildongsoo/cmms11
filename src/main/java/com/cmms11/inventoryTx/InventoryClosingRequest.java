package com.cmms11.inventoryTx;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 이름: InventoryClosingRequest
 * 작성자: codex
 * 작성일: 2025-08-20
 * 수정일:
 * 프로그램 개요: 재고마감 요청 DTO.
 */
public record InventoryClosingRequest(
    LocalDate closingDate,
    String storageId,
    String inventoryId,
    BigDecimal beginQty,
    BigDecimal beginAmount,
    BigDecimal inQty,
    BigDecimal inAmount,
    BigDecimal outQty,
    BigDecimal outAmount,
    BigDecimal moveQty,
    BigDecimal moveAmount,
    BigDecimal adjQty,
    BigDecimal adjAmount,
    BigDecimal endQty,
    BigDecimal endAmount
) {}
