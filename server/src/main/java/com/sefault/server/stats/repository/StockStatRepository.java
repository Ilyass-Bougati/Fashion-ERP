package com.sefault.server.stats.repository;

import com.sefault.server.stats.dto.projection.StockStatProjection;
import com.sefault.server.stats.entity.StockStat;
import com.sefault.server.stats.enums.PeriodType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface StockStatRepository extends JpaRepository<@NonNull StockStat, @NonNull UUID> {
    Optional<StockStatProjection> getStockStatProjectionById(UUID id);

    Optional<StockStat> findByStatDateAndPeriodTypeAndProductVariationSku(
            LocalDate statDate, PeriodType periodType, String productVariationSku);

    Page<StockStatProjection> findByStatDateAndPeriodType(LocalDate statDate, PeriodType periodType, Pageable pageable);

    @Query("SELECT DISTINCT s.productVariationSku FROM StockStat s")
    List<String> findDistinctProductVariationSkus();

    List<StockStat> findTop30ByProductVariationSkuOrderByStatDateDesc(String sku);
}
