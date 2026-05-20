package com.sefault.server.stats.repository;

import com.sefault.server.stats.dto.projection.SalesStatProjection;
import com.sefault.server.stats.entity.SalesStat;
import com.sefault.server.stats.enums.PeriodType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SalesStatRepository extends JpaRepository<@NonNull SalesStat, @NonNull UUID> {
    Optional<SalesStatProjection> getSalesStatProjectionById(UUID id);

    Optional<SalesStat> findByStatDateAndPeriodType(LocalDate statDate, PeriodType periodType);

    List<SalesStatProjection> findByPeriodType(PeriodType periodType);

    Page<SalesStatProjection> findByPeriodType(PeriodType periodType, Pageable pageable);

    List<SalesStat> findTop60ByPeriodTypeOrderByStatDateDesc(PeriodType periodType);
}
