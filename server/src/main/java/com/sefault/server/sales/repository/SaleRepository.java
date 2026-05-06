package com.sefault.server.sales.repository;

import com.sefault.server.sales.dto.projection.SaleProjection;
import com.sefault.server.sales.entity.Sale;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.sefault.server.stats.dto.projection.ProductVariationVelocityProjection;
import com.sefault.server.stats.dto.projection.RevenueAggregationProjection;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SaleRepository extends JpaRepository<@NonNull Sale, @NonNull UUID> {
    Optional<SaleProjection> getSaleProjectionById(UUID id);

    @Query("""
        SELECT COALESCE(SUM((sl.quantity * sl.saleAtPrice) * (1.0 - s.discount)), 0.0)
        FROM Sale s
        JOIN s.saleLines sl
        WHERE s.employeeId = :employeeId
        AND s.createdAt BETWEEN :startDate AND :endDate
        AND s.refunded = false
    """)
    Double sumSaleAmountByEmployeeId(
            @Param("employeeId") UUID employeeId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("""
        SELECT coalesce(SUM((sl.quantity * sl.saleAtPrice) * (1.0 - s.discount)), 0.0)
        FROM Sale s
        JOIN s.saleLines sl
        WHERE s.createdAt BETWEEN :startDate AND :endDate
        AND s.refunded = false
        """)
    Double calculateTotalNetRevenueForPeriod(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("""
            SELECT COUNT(s)
            FROM Sale s
            WHERE s.createdAt between :startDate AND :endDate
            AND s.refunded = false
    """)
    Long countValidTransactions(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("""
            SELECT COUNT(s)
            FROM Sale s
            WHERE s.createdAt between :startDate AND :endDate
            AND s.refunded = true
    """)
    Long countRefundedTransactions(
            @Param("startDate") LocalDateTime start,
            @Param("endDate") LocalDateTime end
    );

    @Query("""
        SELECT
                coalesce(SUM(sl.quantity * sl.saleAtPrice), 0.0) AS grossRevenue,
                coalesce(SUM(sl.quantity * sl.saleAtPrice * (1.0 - s.discount)), 0.0) AS netRevenue,
                coalesce(SUM(sl.quantity), 0) AS unitsSold
        FROM Sale s
        JOIN s.saleLines sl
        WHERE s.createdAt BETWEEN :startDate AND :endDate
        AND s.refunded = false
        """)
    RevenueAggregationProjection calculateRevenueAndUnits(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("""
        SELECT pc.name
        FROM Sale s JOIN s.saleLines sl JOIN sl.productVariation pv JOIN pv.product p JOIN p.productCategory pc
        WHERE s.createdAt BETWEEN :startDate AND :endDate AND s.refunded = false
        GROUP BY pc.id, pc.name
        ORDER BY SUM(sl.quantity) DESC
        LIMIT 1
    """)
    String findTopCategoryNameForPeriod(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("""
        SELECT pv.sku
        FROM Sale s JOIN s.saleLines sl JOIN sl.productVariation pv
        WHERE s.createdAt BETWEEN :startDate AND :endDate AND s.refunded = false
        GROUP BY pv.id, pv.sku
        ORDER BY SUM(sl.quantity) DESC
        LIMIT 1
    """)
    String findTopProductSkuForPeriod(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("""
        SELECT sl.productVariation.id AS productVariationId,
               SUM(sl.quantity) AS unitsSold
        FROM Sale s JOIN s.saleLines sl
        WHERE s.createdAt >= :start AND s.refunded = false
        GROUP BY sl.productVariation.id
    """)
    List<ProductVariationVelocityProjection> getSalesVelocitySince(@Param("start") LocalDateTime start);
}
