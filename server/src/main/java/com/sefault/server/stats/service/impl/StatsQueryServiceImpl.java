package com.sefault.server.stats.service.impl;

import com.sefault.server.stats.dto.projection.EmployeePerformanceStatProjection;
import com.sefault.server.stats.dto.projection.FinancialStatProjection;
import com.sefault.server.stats.dto.projection.SalesStatProjection;
import com.sefault.server.stats.dto.projection.StockStatProjection;
import com.sefault.server.stats.enums.PeriodType;
import com.sefault.server.stats.repository.EmployeePerformanceStatRepository;
import com.sefault.server.stats.repository.FinancialStatRepository;
import com.sefault.server.stats.repository.SalesStatRepository;
import com.sefault.server.stats.repository.StockStatRepository;
import com.sefault.server.stats.service.StatsQueryService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsQueryServiceImpl implements StatsQueryService {

    private final FinancialStatRepository financialRepo;
    private final SalesStatRepository salesRepo;
    private final StockStatRepository stockRepo;
    private final EmployeePerformanceStatRepository employeeRepo;

    public Page<FinancialStatProjection> getFinancialStats(PeriodType periodType, Pageable pageable) {
        return financialRepo.findByPeriodType(periodType, pageable);
    }

    public Page<SalesStatProjection> getSalesStats(PeriodType periodType, Pageable pageable) {
        return salesRepo.findByPeriodType(periodType, pageable);
    }

    public Page<EmployeePerformanceStatProjection> getEmployeePerformanceStats(
            PeriodType periodType, Pageable pageable) {
        return employeeRepo.findByPeriodType(periodType, pageable);
    }

    public Page<StockStatProjection> getStockStats(LocalDate statDate, PeriodType periodType, Pageable pageable) {
        return stockRepo.findByStatDateAndPeriodType(statDate, periodType, pageable);
    }
}
