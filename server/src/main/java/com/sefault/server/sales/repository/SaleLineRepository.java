package com.sefault.server.sales.repository;

import com.sefault.server.sales.dto.projection.SaleLineProjection;
import com.sefault.server.sales.entity.SaleLine;
import com.sefault.server.sales.entity.id.SaleLineId;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SaleLineRepository extends JpaRepository<@NonNull SaleLine, @NonNull SaleLineId> {
    Optional<SaleLineProjection> getSaleLineProjectionById(SaleLineId id);

    List<SaleLineProjection> findAllBySaleId(UUID saleId);

    @Modifying
    @Transactional
    @Query("DELETE FROM SaleLine s WHERE s.id.saleId = :saleId AND s.id.productVariationId = :productVariationId")
    void deleteByCompositeId(@Param("saleId") UUID saleId, @Param("productVariationId") UUID productVariationId);
}
