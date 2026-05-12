package com.sefault.server.prediction.repository;

import com.sefault.server.prediction.entity.EmployeePerformancePrediction;
import com.sefault.server.stats.enums.PeriodType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeePerformancePredictionRepository extends JpaRepository<EmployeePerformancePrediction, UUID> {

    Optional<EmployeePerformancePrediction> findByTargetDateAndPeriodTypeAndEmployeeCinAndModelVersion(
            LocalDate targetDate, PeriodType periodType, String cin, String modelVersion);

    Page<EmployeePerformancePrediction> findByTargetDateGreaterThanEqualAndPeriodTypeAndEmployeeCinOrderByTargetDateAsc(
            LocalDate targetDate, PeriodType periodType, String cin, Pageable pageable);
}