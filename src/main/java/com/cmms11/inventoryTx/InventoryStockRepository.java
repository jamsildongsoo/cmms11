package com.cmms11.inventoryTx;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 이름: InventoryStockRepository
 * 작성자: codex
 * 작성일: 2025-08-20
 * 수정일:
 * 프로그램 개요: 재고현황 데이터 접근 계층.
 */
@Repository
public interface InventoryStockRepository extends JpaRepository<InventoryStock, InventoryStockId> {

    /**
     * 회사별 재고현황 조회
     */
    Page<InventoryStock> findByIdCompanyId(String companyId, Pageable pageable);

    /**
     * 회사별, 재고번호별 재고현황 조회
     */
    Page<InventoryStock> findByIdCompanyIdAndIdInventoryId(String companyId, String inventoryId, Pageable pageable);

    /**
     * 회사별, 창고번호별 재고현황 조회
     */
    Page<InventoryStock> findByIdCompanyIdAndIdStorageId(String companyId, String storageId, Pageable pageable);

    /**
     * 회사별, 재고번호별, 창고번호별 재고현황 조회
     */
    Optional<InventoryStock> findByIdCompanyIdAndIdInventoryIdAndIdStorageId(
            String companyId, String inventoryId, String storageId);

    /**
     * 회사별, 재고번호별, 창고번호별 재고현황 존재 여부 확인
     */
    boolean existsByIdCompanyIdAndIdInventoryIdAndIdStorageId(
            String companyId, String inventoryId, String storageId);

    /**
     * 회사별, 재고번호별, 창고번호별 재고현황 삭제
     */
    void deleteByIdCompanyIdAndIdInventoryIdAndIdStorageId(
            String companyId, String inventoryId, String storageId);

    /**
     * 회사별, 재고번호별, 창고번호별 재고 수량 조회
     */
    @Query("SELECT COALESCE(s.qty, 0) FROM InventoryStock s " +
           "WHERE s.id.companyId = :companyId " +
           "AND s.id.inventoryId = :inventoryId " +
           "AND s.id.storageId = :storageId")
    BigDecimal findQtyByIdCompanyIdAndIdInventoryIdAndIdStorageId(
            @Param("companyId") String companyId,
            @Param("inventoryId") String inventoryId,
            @Param("storageId") String storageId);

    /**
     * 회사별, 재고번호별, 창고번호별 재고 금액 조회
     */
    @Query("SELECT COALESCE(s.amount, 0) FROM InventoryStock s " +
           "WHERE s.id.companyId = :companyId " +
           "AND s.id.inventoryId = :inventoryId " +
           "AND s.id.storageId = :storageId")
    BigDecimal findAmountByIdCompanyIdAndIdInventoryIdAndIdStorageId(
            @Param("companyId") String companyId,
            @Param("inventoryId") String inventoryId,
            @Param("storageId") String storageId);

    /**
     * 회사별, 재고번호별, 창고번호별 재고 수량 업데이트
     */
    @Query("UPDATE InventoryStock s SET s.qty = :qty, s.updatedAt = CURRENT_TIMESTAMP, s.updatedBy = :updatedBy " +
           "WHERE s.id.companyId = :companyId " +
           "AND s.id.inventoryId = :inventoryId " +
           "AND s.id.storageId = :storageId")
    int updateQtyByIdCompanyIdAndIdInventoryIdAndIdStorageId(
            @Param("companyId") String companyId,
            @Param("inventoryId") String inventoryId,
            @Param("storageId") String storageId,
            @Param("qty") Double qty,
            @Param("updatedBy") String updatedBy);

    /**
     * 회사별, 재고번호별, 창고번호별 재고 금액 업데이트
     */
    @Query("UPDATE InventoryStock s SET s.amount = :amount, s.updatedAt = CURRENT_TIMESTAMP, s.updatedBy = :updatedBy " +
           "WHERE s.id.companyId = :companyId " +
           "AND s.id.inventoryId = :inventoryId " +
           "AND s.id.storageId = :storageId")
    int updateAmountByIdCompanyIdAndIdInventoryIdAndIdStorageId(
            @Param("companyId") String companyId,
            @Param("inventoryId") String inventoryId,
            @Param("storageId") String storageId,
            @Param("amount") Double amount,
            @Param("updatedBy") String updatedBy);

    /**
     * 회사별, 재고번호별, 창고번호별 재고 수량 및 금액 업데이트
     */
    @Query("UPDATE InventoryStock s SET s.qty = :qty, s.amount = :amount, s.updatedAt = CURRENT_TIMESTAMP, s.updatedBy = :updatedBy " +
           "WHERE s.id.companyId = :companyId " +
           "AND s.id.inventoryId = :inventoryId " +
           "AND s.id.storageId = :storageId")
    int updateQtyAndAmountByIdCompanyIdAndIdInventoryIdAndIdStorageId(
            @Param("companyId") String companyId,
            @Param("inventoryId") String inventoryId,
            @Param("storageId") String storageId,
            @Param("qty") Double qty,
            @Param("amount") Double amount,
            @Param("updatedBy") String updatedBy);

    /**
     * 회사별, 재고번호별, 창고번호별 재고 수량 증가
     */
    @Query("UPDATE InventoryStock s SET s.qty = s.qty + :qty, s.updatedAt = CURRENT_TIMESTAMP, s.updatedBy = :updatedBy " +
           "WHERE s.id.companyId = :companyId " +
           "AND s.id.inventoryId = :inventoryId " +
           "AND s.id.storageId = :storageId")
    int increaseQtyByIdCompanyIdAndIdInventoryIdAndIdStorageId(
            @Param("companyId") String companyId,
            @Param("inventoryId") String inventoryId,
            @Param("storageId") String storageId,
            @Param("qty") Double qty,
            @Param("updatedBy") String updatedBy);

    /**
     * 회사별, 재고번호별, 창고번호별 재고 수량 감소
     */
    @Query("UPDATE InventoryStock s SET s.qty = s.qty - :qty, s.updatedAt = CURRENT_TIMESTAMP, s.updatedBy = :updatedBy " +
           "WHERE s.id.companyId = :companyId " +
           "AND s.id.inventoryId = :inventoryId " +
           "AND s.id.storageId = :storageId")
    int decreaseQtyByIdCompanyIdAndIdInventoryIdAndIdStorageId(
            @Param("companyId") String companyId,
            @Param("inventoryId") String inventoryId,
            @Param("storageId") String storageId,
            @Param("qty") Double qty,
            @Param("updatedBy") String updatedBy);

    /**
     * 회사별, 재고번호별, 창고번호별 재고 금액 증가
     */
    @Query("UPDATE InventoryStock s SET s.amount = s.amount + :amount, s.updatedAt = CURRENT_TIMESTAMP, s.updatedBy = :updatedBy " +
           "WHERE s.id.companyId = :companyId " +
           "AND s.id.inventoryId = :inventoryId " +
           "AND s.id.storageId = :storageId")
    int increaseAmountByIdCompanyIdAndIdInventoryIdAndIdStorageId(
            @Param("companyId") String companyId,
            @Param("inventoryId") String inventoryId,
            @Param("storageId") String storageId,
            @Param("amount") Double amount,
            @Param("updatedBy") String updatedBy);

    /**
     * 회사별, 재고번호별, 창고번호별 재고 금액 감소
     */
    @Query("UPDATE InventoryStock s SET s.amount = s.amount - :amount, s.updatedAt = CURRENT_TIMESTAMP, s.updatedBy = :updatedBy " +
           "WHERE s.id.companyId = :companyId " +
           "AND s.id.inventoryId = :inventoryId " +
           "AND s.id.storageId = :storageId")
    int decreaseAmountByIdCompanyIdAndIdInventoryIdAndIdStorageId(
            @Param("companyId") String companyId,
            @Param("inventoryId") String inventoryId,
            @Param("storageId") String storageId,
            @Param("amount") Double amount,
            @Param("updatedBy") String updatedBy);

    /**
     * 회사별, 재고번호별, 창고번호별 재고 수량 및 금액 증가
     */
    @Query("UPDATE InventoryStock s SET s.qty = s.qty + :qty, s.amount = s.amount + :amount, s.updatedAt = CURRENT_TIMESTAMP, s.updatedBy = :updatedBy " +
           "WHERE s.id.companyId = :companyId " +
           "AND s.id.inventoryId = :inventoryId " +
           "AND s.id.storageId = :storageId")
    int increaseQtyAndAmountByIdCompanyIdAndIdInventoryIdAndIdStorageId(
            @Param("companyId") String companyId,
            @Param("inventoryId") String inventoryId,
            @Param("storageId") String storageId,
            @Param("qty") Double qty,
            @Param("amount") Double amount,
            @Param("updatedBy") String updatedBy);

    /**
     * 회사별, 재고번호별, 창고번호별 재고 수량 및 금액 감소
     */
    @Query("UPDATE InventoryStock s SET s.qty = s.qty - :qty, s.amount = s.amount - :amount, s.updatedAt = CURRENT_TIMESTAMP, s.updatedBy = :updatedBy " +
           "WHERE s.id.companyId = :companyId " +
           "AND s.id.inventoryId = :inventoryId " +
           "AND s.id.storageId = :storageId")
    int decreaseQtyAndAmountByIdCompanyIdAndIdInventoryIdAndIdStorageId(
            @Param("companyId") String companyId,
            @Param("inventoryId") String inventoryId,
            @Param("storageId") String storageId,
            @Param("qty") Double qty,
            @Param("amount") Double amount,
            @Param("updatedBy") String updatedBy);

    /**
     * 회사별, 재고번호별, 창고번호별 재고 수량 및 금액 조회 (존재하지 않으면 0 반환)
     */
    @Query("SELECT COALESCE(s.qty, 0), COALESCE(s.amount, 0) FROM InventoryStock s " +
           "WHERE s.id.companyId = :companyId " +
           "AND s.id.inventoryId = :inventoryId " +
           "AND s.id.storageId = :storageId")
    List<Object[]> findQtyAndAmountByIdCompanyIdAndIdInventoryIdAndIdStorageId(
            @Param("companyId") String companyId,
            @Param("inventoryId") String inventoryId,
            @Param("storageId") String storageId);

    /**
     * 회사별, 재고번호별, 창고번호별 재고 수량 및 금액 조회 (존재하지 않으면 0 반환)
     */
    @Query("SELECT COALESCE(s.qty, 0), COALESCE(s.amount, 0) FROM InventoryStock s " +
           "WHERE s.id.companyId = :companyId " +
           "AND s.id.inventoryId = :inventoryId " +
           "AND s.id.storageId = :storageId")
    Optional<Object[]> findQtyAndAmountOptionalByIdCompanyIdAndIdInventoryIdAndIdStorageId(
            @Param("companyId") String companyId,
            @Param("inventoryId") String inventoryId,
            @Param("storageId") String storageId);
}
