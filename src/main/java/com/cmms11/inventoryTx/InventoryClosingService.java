package com.cmms11.inventoryTx;

import com.cmms11.common.seq.AutoNumberService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 이름: InventoryClosingService
 * 작성자: codex
 * 작성일: 2025-08-20
 * 수정일:
 * 프로그램 개요: 재고마감 비즈니스 로직을 처리하는 서비스.
 */
@Service
@Transactional
public class InventoryClosingService {

    private final InventoryHistoryRepository historyRepository;
    private final InventoryClosingRepository closingRepository;
    private final AutoNumberService autoNumberService;

    public InventoryClosingService(
            InventoryHistoryRepository historyRepository,
            InventoryClosingRepository closingRepository,
            AutoNumberService autoNumberService) {
        this.historyRepository = historyRepository;
        this.closingRepository = closingRepository;
        this.autoNumberService = autoNumberService;
    }

    /**
     * 월별 마감 처리
     */
    public InventoryClosingResponse processMonthlyClosing(InventoryClosingRequest request) {
        String companyId = "C0001"; // TODO: 실제 회사 ID로 변경
        
        // 1. 마감 데이터 존재 여부 확인
        validateClosingNotExists(companyId, request.storageId(), request.inventoryId(), request.closingDate());
        
        // 2. 마감 데이터 생성
        InventoryClosing closing = createClosingData(companyId, request);
        closingRepository.save(closing);
        
        // 3. 응답 생성
        return createClosingResponse(closing);
    }

    /**
     * 마감 데이터 존재 여부 확인
     */
    private void validateClosingNotExists(String companyId, String storageId, String inventoryId, LocalDate closingDate) {
        // TODO: 마감 데이터 존재 여부 확인 로직 구현
        // 이미 마감된 데이터가 있는지 확인
    }

    /**
     * 마감 데이터 생성
     */
    private InventoryClosing createClosingData(String companyId, InventoryClosingRequest request) {
        String closingId = autoNumberService.generateTxId(companyId, "C", request.closingDate());
        
        InventoryClosing closing = new InventoryClosing();
        String yyyymm = request.closingDate().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMM"));
        closing.setId(new InventoryClosingId(companyId, yyyymm, request.storageId(), request.inventoryId()));
        closing.setBeginQty(request.beginQty());
        closing.setBeginAmount(request.beginAmount());
        closing.setInQty(request.inQty());
        closing.setInAmount(request.inAmount());
        closing.setOutQty(request.outQty());
        closing.setOutAmount(request.outAmount());
        closing.setMoveQty(request.moveQty());
        closing.setMoveAmount(request.moveAmount());
        closing.setAdjQty(request.adjQty());
        closing.setAdjAmount(request.adjAmount());
        closing.setEndQty(request.endQty());
        closing.setEndAmount(request.endAmount());
        closing.setStatus("CLOSED");
        closing.setClosedAt(LocalDateTime.now());
        closing.setClosedBy("SYSTEM"); // TODO: 실제 사용자 ID로 변경
        
        return closing;
    }

    /**
     * 마감 응답 생성
     */
    private InventoryClosingResponse createClosingResponse(InventoryClosing closing) {
        return new InventoryClosingResponse(
                closing.getId().getYyyymm(),
                closing.getId().getStorageId(),
                closing.getId().getInventoryId(),
                closing.getBeginQty(),
                closing.getBeginAmount(),
                closing.getInQty(),
                closing.getInAmount(),
                closing.getOutQty(),
                closing.getOutAmount(),
                closing.getMoveQty(),
                closing.getMoveAmount(),
                closing.getAdjQty(),
                closing.getAdjAmount(),
                closing.getEndQty(),
                closing.getEndAmount(),
                closing.getStatus(),
                closing.getClosedAt(),
                closing.getClosedBy()
        );
    }

    /**
     * 기초재고 계산
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateBeginStock(String companyId, String storageId, String inventoryId, LocalDate beginDate) {
        // 전월 마감 데이터에서 기초재고 조회
        // TODO: 실제 기초재고 계산 로직 구현
        return BigDecimal.ZERO;
    }

    /**
     * 입고 수량 집계
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateInboundQty(String companyId, String storageId, String inventoryId, LocalDate fromDate, LocalDate toDate) {
        BigDecimal result = historyRepository.sumInQtyByCompanyIdAndInventoryIdAndStorageIdAndTxDateBetween(
                companyId, inventoryId, storageId, fromDate, toDate);
        return result != null ? result : BigDecimal.ZERO;
    }

    /**
     * 출고 수량 집계
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateOutboundQty(String companyId, String storageId, String inventoryId, LocalDate fromDate, LocalDate toDate) {
        BigDecimal result = historyRepository.sumOutQtyByCompanyIdAndInventoryIdAndStorageIdAndTxDateBetween(
                companyId, inventoryId, storageId, fromDate, toDate);
        return result != null ? result : BigDecimal.ZERO;
    }

    /**
     * 이동 수량 집계
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateMoveQty(String companyId, String storageId, String inventoryId, LocalDate fromDate, LocalDate toDate) {
        BigDecimal result = historyRepository.sumQtyByCompanyIdAndInventoryIdAndStorageIdAndTxTypeAndTxDateBetween(
                companyId, inventoryId, storageId, "MOVE", fromDate, toDate);
        return result != null ? result : BigDecimal.ZERO;
    }

    /**
     * 조정 수량 집계
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateAdjustmentQty(String companyId, String storageId, String inventoryId, LocalDate fromDate, LocalDate toDate) {
        BigDecimal result = historyRepository.sumQtyByCompanyIdAndInventoryIdAndStorageIdAndTxTypeAndTxDateBetween(
                companyId, inventoryId, storageId, "ADJ", fromDate, toDate);
        return result != null ? result : BigDecimal.ZERO;
    }

    /**
     * 입고 금액 집계
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateInboundAmount(String companyId, String storageId, String inventoryId, LocalDate fromDate, LocalDate toDate) {
        BigDecimal result = historyRepository.sumInAmountByCompanyIdAndInventoryIdAndStorageIdAndTxDateBetween(
                companyId, inventoryId, storageId, fromDate, toDate);
        return result != null ? result : BigDecimal.ZERO;
    }

    /**
     * 출고 금액 집계
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateOutboundAmount(String companyId, String storageId, String inventoryId, LocalDate fromDate, LocalDate toDate) {
        BigDecimal result = historyRepository.sumOutAmountByCompanyIdAndInventoryIdAndStorageIdAndTxDateBetween(
                companyId, inventoryId, storageId, fromDate, toDate);
        return result != null ? result : BigDecimal.ZERO;
    }

    /**
     * 이동 금액 집계
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateMoveAmount(String companyId, String storageId, String inventoryId, LocalDate fromDate, LocalDate toDate) {
        BigDecimal result = historyRepository.sumAmountByCompanyIdAndInventoryIdAndStorageIdAndTxTypeAndTxDateBetween(
                companyId, inventoryId, storageId, "MOVE", fromDate, toDate);
        return result != null ? result : BigDecimal.ZERO;
    }

    /**
     * 조정 금액 집계
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateAdjustmentAmount(String companyId, String storageId, String inventoryId, LocalDate fromDate, LocalDate toDate) {
        BigDecimal result = historyRepository.sumAmountByCompanyIdAndInventoryIdAndStorageIdAndTxTypeAndTxDateBetween(
                companyId, inventoryId, storageId, "ADJ", fromDate, toDate);
        return result != null ? result : BigDecimal.ZERO;
    }

    /**
     * 마감 요약 계산
     */
    @Transactional(readOnly = true)
    public ClosingSummary calculateClosingSummary(String companyId, String storageId, String inventoryId, LocalDate closingDate) {
        LocalDate beginDate = closingDate.withDayOfMonth(1);
        LocalDate endDate = closingDate.withDayOfMonth(closingDate.lengthOfMonth());
        
        BigDecimal beginQty = calculateBeginStock(companyId, storageId, inventoryId, beginDate);
        BigDecimal beginAmount = BigDecimal.ZERO; // TODO: 기초금액 계산
        
        BigDecimal inQty = calculateInboundQty(companyId, storageId, inventoryId, beginDate, endDate);
        BigDecimal inAmount = calculateInboundAmount(companyId, storageId, inventoryId, beginDate, endDate);
        
        BigDecimal outQty = calculateOutboundQty(companyId, storageId, inventoryId, beginDate, endDate);
        BigDecimal outAmount = calculateOutboundAmount(companyId, storageId, inventoryId, beginDate, endDate);
        
        BigDecimal moveQty = calculateMoveQty(companyId, storageId, inventoryId, beginDate, endDate);
        BigDecimal moveAmount = calculateMoveAmount(companyId, storageId, inventoryId, beginDate, endDate);
        
        BigDecimal adjQty = calculateAdjustmentQty(companyId, storageId, inventoryId, beginDate, endDate);
        BigDecimal adjAmount = calculateAdjustmentAmount(companyId, storageId, inventoryId, beginDate, endDate);
        
        // 기말 수량 및 금액 계산
        BigDecimal endQty = beginQty.add(inQty).subtract(outQty).add(moveQty).add(adjQty);
        BigDecimal endAmount = beginAmount.add(inAmount).subtract(outAmount).add(moveAmount).add(adjAmount);
        
        return new ClosingSummary(
                beginQty, beginAmount,
                inQty, inAmount,
                outQty, outAmount,
                moveQty, moveAmount,
                adjQty, adjAmount,
                endQty, endAmount
        );
    }

    /**
     * 마감 이력 조회
     */
    @Transactional(readOnly = true)
    public List<InventoryClosingResponse> getClosingHistory(String companyId, String closingMonth) {
        // TODO: 마감 이력 조회 로직 구현
        return List.of();
    }

    /**
     * 마감 상세 조회
     */
    @Transactional(readOnly = true)
    public Optional<InventoryClosingResponse> getClosingDetail(String companyId, String closingId) {
        // TODO: 마감 상세 조회 로직 구현
        return Optional.empty();
    }

    /**
     * 마감 요약 DTO
     */
    public record ClosingSummary(
            BigDecimal beginQty, BigDecimal beginAmount,
            BigDecimal inQty, BigDecimal inAmount,
            BigDecimal outQty, BigDecimal outAmount,
            BigDecimal moveQty, BigDecimal moveAmount,
            BigDecimal adjQty, BigDecimal adjAmount,
            BigDecimal endQty, BigDecimal endAmount
    ) {}
}
