package com.sefault.server.sales.repository;

import com.sefault.server.sales.dto.projection.SaleProjection;
import com.sefault.server.sales.entity.Sale;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SaleRepository extends JpaRepository<@NonNull Sale, @NonNull UUID> {
    Optional<SaleProjection> getSaleProjectionById(UUID id);

    @Query("""
        SELECT COALESCE(SUM(), 0)
        FROM Sale s
        WHERE s.employeeId = :employeeId
        AND s.createdAt BETWEEN :startDate AND :endDate
    """)
    Double sumSaleAmountByEmployeeId(
            @Param("employeeId") UUID employeeId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
