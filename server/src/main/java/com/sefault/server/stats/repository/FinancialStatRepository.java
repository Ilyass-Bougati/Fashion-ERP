package com.sefault.server.stats.repository;

import com.sefault.server.stats.dto.projection.FinancialStatProjection;
import com.sefault.server.stats.entity.FinancialStat;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import com.sefault.server.stats.entity.SalesStat;
import com.sefault.server.stats.enums.PeriodType;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FinancialStatRepository extends JpaRepository<@NonNull FinancialStat, @NonNull UUID> {
    Optional<FinancialStatProjection> getFinancialStatProjectionById(UUID id);

    Optional<FinancialStat> findByStatDateAndPeriodType(LocalDate statDate, PeriodType periodType);
}
