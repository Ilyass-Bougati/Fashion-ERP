package com.sefault.server.stats.service;

import com.sefault.server.stats.dto.projection.EmployeePerformanceStatProjection;
import com.sefault.server.stats.dto.projection.FinancialStatProjection;
import com.sefault.server.stats.dto.projection.SalesStatProjection;
import com.sefault.server.stats.dto.projection.StockStatProjection;
import com.sefault.server.stats.enums.PeriodType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface StatsQueryService {
    Page<FinancialStatProjection> getFinancialStats(PeriodType periodType, Pageable pageable);

    Page<SalesStatProjection> getSalesStats(PeriodType periodType, Pageable pageable);

    Page<EmployeePerformanceStatProjection> getEmployeePerformanceStats(PeriodType periodType, Pageable pageable);

    Page<StockStatProjection> getStockStats(LocalDate statDate, PeriodType periodType, Pageable pageable);
}
