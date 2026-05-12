package com.sefault.server.prediction.service;

import com.sefault.server.prediction.dto.record.*;
import com.sefault.server.stats.enums.PeriodType;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PredictionQueryService {
    Page<SalesPredictionRecord> getSalesPredictions(LocalDate targetDateFrom, PeriodType periodType, Pageable pageable);

    Page<StockPredictionRecord> getStockPredictions(
            LocalDate targetDateFrom, PeriodType periodType, String sku, Pageable pageable);

    Page<FinancialPredictionRecord> getFinancialPredictions(
            LocalDate targetDateFrom, PeriodType periodType, Pageable pageable);

    Page<EmployeePerformancePredictionRecord> getEmployeePredictions(
            LocalDate targetDateFrom, PeriodType periodType, String cin, Pageable pageable);
}
