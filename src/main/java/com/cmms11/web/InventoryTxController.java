package com.cmms11.web;

import com.cmms11.inventoryTx.InventoryTxService;
import com.cmms11.inventoryTx.InventoryClosingService;
import com.cmms11.inventoryTx.InventoryLedgerService;
import com.cmms11.inventoryTx.InventoryTxRequest;
import com.cmms11.inventoryTx.InventoryTxResponse;
import com.cmms11.inventoryTx.InventoryClosingRequest;
import com.cmms11.inventoryTx.InventoryClosingResponse;
import com.cmms11.inventoryTx.InventoryLedgerResponse;
import com.cmms11.inventoryTx.InventoryStockResponse;
import com.cmms11.domain.storage.StorageService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * 이름: InventoryTxController
 * 작성자: codex
 * 작성일: 2025-08-20
 * 수정일:
 * 프로그램 개요: 재고거래 웹 화면 및 API 엔드포인트를 제공하는 컨트롤러.
 */
@Controller
public class InventoryTxController {

    private final InventoryTxService inventoryTxService;
    private final InventoryClosingService inventoryClosingService;
    private final InventoryLedgerService inventoryLedgerService;
    private final StorageService storageService;

    public InventoryTxController(
            InventoryTxService inventoryTxService,
            InventoryClosingService inventoryClosingService,
            InventoryLedgerService inventoryLedgerService,
            StorageService storageService) {
        this.inventoryTxService = inventoryTxService;
        this.inventoryClosingService = inventoryClosingService;
        this.inventoryLedgerService = inventoryLedgerService;
        this.storageService = storageService;
    }

    // 웹 컨트롤러 화면 제공
    @GetMapping("/inventoryTx/transaction")
    public String transactionForm(Model model) {
        model.addAttribute("txTypes", List.of("IN", "OUT", "MOVE", "ADJ"));
        addReferenceData(model);
        return "inventoryTx/transaction";
    }

    @GetMapping("/inventoryTx/closing")
    public String closingForm(Model model) {
        return "inventoryTx/closing";
    }

    @GetMapping("/inventoryTx/ledger")
    public String ledgerForm(Model model) {
        return "inventoryTx/ledger";
    }

    // 거래 처리 API
    @PostMapping("/api/inventoryTx/transaction")
    @ResponseBody
    public ResponseEntity<InventoryTxResponse> processTransaction(@RequestBody InventoryTxRequest request) {
        try {
            InventoryTxResponse response = inventoryTxService.processTransaction(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 월별 마감 처리 API
    @PostMapping("/api/inventoryTx/closing")
    @ResponseBody
    public ResponseEntity<InventoryClosingResponse> processClosing(@RequestBody InventoryClosingRequest request) {
        try {
            InventoryClosingResponse response = inventoryClosingService.processMonthlyClosing(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 원장 조회 API
    @GetMapping("/api/inventoryTx/ledger")
    @ResponseBody
    public ResponseEntity<List<InventoryLedgerResponse>> getLedger(
            @RequestParam String companyId,
            @RequestParam String storageId,
            @RequestParam String inventoryId,
            @RequestParam String fromDate,
            @RequestParam String toDate) {
        try {
            InventoryLedgerService.LedgerSearchRequest searchRequest = new InventoryLedgerService.LedgerSearchRequest(
                    storageId, inventoryId, LocalDate.parse(fromDate), LocalDate.parse(toDate));
            List<InventoryLedgerResponse> ledger = inventoryLedgerService.getLedger(searchRequest);
            return ResponseEntity.ok(ledger);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 재고 조회 API
    @GetMapping("/api/inventoryTx/stock")
    @ResponseBody
    public ResponseEntity<InventoryStockResponse> getStock(
            @RequestParam String companyId,
            @RequestParam String storageId,
            @RequestParam String inventoryId) {
        try {
            InventoryStockResponse stock = inventoryTxService.getCurrentStock(companyId, inventoryId, storageId);
            return ResponseEntity.ok(stock);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 마감 조회 API
    @GetMapping("/api/inventoryTx/closing")
    @ResponseBody
    public ResponseEntity<List<InventoryClosingResponse>> getClosing(
            @RequestParam String companyId,
            @RequestParam String closingMonth,
            @RequestParam(required = false) String storageId,
            @RequestParam(required = false) String inventoryId) {
        try {
            List<InventoryClosingResponse> closing = inventoryClosingService.getClosingHistory(companyId, closingMonth);
            return ResponseEntity.ok(closing);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    private void addReferenceData(Model model) {
        // 창고 목록
        try {
            model.addAttribute("storages", storageService.list(null, Pageable.unpaged()).getContent());
        } catch (Exception e) {
            model.addAttribute("storages", List.of());
        }
    }
}
