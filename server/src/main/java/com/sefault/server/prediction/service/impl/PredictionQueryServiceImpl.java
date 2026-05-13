package com.sefault.server.prediction.service.impl;

import com.sefault.server.prediction.dto.record.*;
import com.sefault.server.prediction.mapper.*;
import com.sefault.server.prediction.repository.*;
import com.sefault.server.prediction.service.PredictionQueryService;
import com.sefault.server.stats.enums.PeriodType;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PredictionQueryServiceImpl implements PredictionQueryService {

    private final SalesPredictionRepository salesRepo;
    private final StockPredictionRepository stockRepo;
    private final FinancialPredictionRepository financialRepo;
    private final EmployeePerformancePredictionRepository employeeRepo;

    private final SalesPredictionMapper salesMapper;
    private final StockPredictionMapper stockMapper;
    private final FinancialPredictionMapper financialMapper;
    private final EmployeePerformancePredictionMapper employeeMapper;

    @Override
    public Page<SalesPredictionRecord> getSalesPredictions(
            LocalDate targetDateFrom, PeriodType periodType, Pageable pageable) {
        return salesRepo
                .findByTargetDateGreaterThanEqualAndPeriodTypeOrderByTargetDateAsc(targetDateFrom, periodType, pageable)
                .map(salesMapper::entityToRecord);
    }

    @Override
    public Page<StockPredictionRecord> getStockPredictions(
            LocalDate targetDateFrom, PeriodType periodType, String sku, Pageable pageable) {
        return stockRepo
                .findByTargetDateGreaterThanEqualAndPeriodTypeAndProductVariationSkuOrderByTargetDateAsc(
                        targetDateFrom, periodType, sku, pageable)
                .map(stockMapper::entityToRecord);
    }

    @Override
    public Page<FinancialPredictionRecord> getFinancialPredictions(
            LocalDate targetDateFrom, PeriodType periodType, Pageable pageable) {
        return financialRepo
                .findByTargetDateGreaterThanEqualAndPeriodTypeOrderByTargetDateAsc(targetDateFrom, periodType, pageable)
                .map(financialMapper::entityToRecord);
    }

    @Override
    public Page<EmployeePerformancePredictionRecord> getEmployeePredictions(
            LocalDate targetDateFrom, PeriodType periodType, String cin, Pageable pageable) {
        return employeeRepo
                .findByTargetDateGreaterThanEqualAndPeriodTypeAndEmployeeCinOrderByTargetDateAsc(
                        targetDateFrom, periodType, cin, pageable)
                .map(employeeMapper::entityToRecord);
    }
}
