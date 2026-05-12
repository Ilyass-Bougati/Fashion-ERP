package com.sefault.server.prediction.repository;

import com.sefault.server.prediction.entity.StockPrediction;
import com.sefault.server.stats.enums.PeriodType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StockPredictionRepository extends JpaRepository<StockPrediction, UUID> {

    Optional<StockPrediction> findByTargetDateAndPeriodTypeAndProductVariationSkuAndModelVersion(
            LocalDate targetDate, PeriodType periodType, String sku, String modelVersion);

    Page<StockPrediction> findByTargetDateGreaterThanEqualAndPeriodTypeAndProductVariationSkuOrderByTargetDateAsc(
            LocalDate targetDate, PeriodType periodType, String sku, Pageable pageable);
}