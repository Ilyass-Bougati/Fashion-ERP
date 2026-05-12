package com.sefault.server.prediction.repository;

import com.sefault.server.prediction.entity.FinancialPrediction;
import com.sefault.server.stats.enums.PeriodType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FinancialPredictionRepository extends JpaRepository<FinancialPrediction, UUID> {

    Optional<FinancialPrediction> findByTargetDateAndPeriodTypeAndModelVersion(
            LocalDate targetDate, PeriodType periodType, String modelVersion);

    Page<FinancialPrediction> findByTargetDateGreaterThanEqualAndPeriodTypeOrderByTargetDateAsc(
            LocalDate targetDate, PeriodType periodType, Pageable pageable);
}