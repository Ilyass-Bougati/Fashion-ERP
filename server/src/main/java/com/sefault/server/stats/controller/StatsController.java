package com.sefault.server.stats.controller;

import com.sefault.server.stats.dto.projection.EmployeePerformanceStatProjection;
import com.sefault.server.stats.dto.projection.FinancialStatProjection;
import com.sefault.server.stats.dto.projection.SalesStatProjection;
import com.sefault.server.stats.dto.projection.StockStatProjection;
import com.sefault.server.stats.enums.PeriodType;
import com.sefault.server.stats.service.StatsQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;

@RestController
@RequestMapping("/api/v1/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsQueryService statsQueryService;

    @GetMapping("/financial")
    public ResponseEntity<Page<FinancialStatProjection>> getFinancialStats(
            @RequestParam PeriodType periodType,
            @PageableDefault(sort = "statDate", direction = DESC) Pageable pageable) {

        return ResponseEntity.ok(statsQueryService.getFinancialStats(periodType, pageable));
    }

    @GetMapping("/sales")
    public ResponseEntity<Page<SalesStatProjection>> getSalesStats(
            @RequestParam PeriodType periodType,
            @PageableDefault(sort = "statDate", direction = DESC) Pageable pageable) {

        return ResponseEntity.ok(statsQueryService.getSalesStats(periodType, pageable));
    }

    @GetMapping("/employees")
    public ResponseEntity<Page<EmployeePerformanceStatProjection>> getEmployeePerformanceStats(
            @RequestParam PeriodType periodType,
            @PageableDefault(sort = "grossSalesAmount", direction = DESC) Pageable pageable) {

        return ResponseEntity.ok(statsQueryService.getEmployeePerformanceStats(periodType, pageable));
    }

    @GetMapping("/stock")
    public ResponseEntity<Page<StockStatProjection>> getStockStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate statDate,
            @RequestParam PeriodType periodType,
            @PageableDefault(sort = "quantityOnHand", direction = ASC) Pageable pageable) {

        return ResponseEntity.ok(statsQueryService.getStockStats(statDate, periodType, pageable));
    }
}