package com.sefault.server.prediction.controller;

import com.sefault.server.prediction.dto.record.*;
import com.sefault.server.prediction.service.PredictionQueryService;
import com.sefault.server.stats.enums.PeriodType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/predictions")
@RequiredArgsConstructor
public class PredictionController {

    private final PredictionQueryService predictionQueryService;

    @GetMapping("/sales")
    public ResponseEntity<Page<SalesPredictionRecord>> getSalesPredictions(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam PeriodType periodType,
            @PageableDefault(size = 30) Pageable pageable) {
        return ResponseEntity.ok(predictionQueryService.getSalesPredictions(fromDate, periodType, pageable));
    }

    @GetMapping("/stock")
    public ResponseEntity<Page<StockPredictionRecord>> getStockPredictions(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam PeriodType periodType,
            @RequestParam String sku,
            @PageableDefault(size = 30) Pageable pageable) {
        return ResponseEntity.ok(predictionQueryService.getStockPredictions(fromDate, periodType, sku, pageable));
    }

    @GetMapping("/financial")
    public ResponseEntity<Page<FinancialPredictionRecord>> getFinancialPredictions(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam PeriodType periodType,
            @PageableDefault(size = 12) Pageable pageable) {
        return ResponseEntity.ok(predictionQueryService.getFinancialPredictions(fromDate, periodType, pageable));
    }

    @GetMapping("/employees")
    public ResponseEntity<Page<EmployeePerformancePredictionRecord>> getEmployeePredictions(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam PeriodType periodType,
            @RequestParam String cin,
            @PageableDefault(size = 30) Pageable pageable) {
        return ResponseEntity.ok(predictionQueryService.getEmployeePredictions(fromDate, periodType, cin, pageable));
    }
}