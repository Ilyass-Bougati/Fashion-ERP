package com.sefault.server.stats.controller;

import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;

import com.sefault.server.stats.dto.projection.EmployeePerformanceStatProjection;
import com.sefault.server.stats.dto.projection.FinancialStatProjection;
import com.sefault.server.stats.dto.projection.SalesStatProjection;
import com.sefault.server.stats.dto.projection.StockStatProjection;
import com.sefault.server.stats.enums.PeriodType;
import com.sefault.server.stats.service.EmployeePerformanceStatService;
import com.sefault.server.stats.service.FinancialStatsService;
import com.sefault.server.stats.service.SalesStatsService;
import com.sefault.server.stats.service.StatsQueryService;
import com.sefault.server.stats.service.StockStatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/stats")
@RequiredArgsConstructor
@Tag(
        name = "Statistics - Dashboards",
        description = "Endpoints for retrieving aggregated ERP statistics, leaderboards, and historical snapshots.")
public class StatsController {

    private final StatsQueryService statsQueryService;
    private final SalesStatsService salesStatsService;
    private final StockStatService stockStatService;
    private final EmployeePerformanceStatService employeePerformanceStatService;
    private final FinancialStatsService financialStatsService;

    @Operation(
            summary = "Get financial statistics",
            description =
                    "Fetch paginated financial summaries (revenue, payroll, fixed charges, net profit). Note: Financial stats are typically only available for MONTHLY and YEARLY periods.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Financial statistics retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid period type requested")
    })
    @GetMapping("/financial")
    public ResponseEntity<Page<FinancialStatProjection>> getFinancialStats(
            @RequestParam PeriodType periodType,
            @PageableDefault(sort = "statDate", direction = DESC) Pageable pageable) {

        return ResponseEntity.ok(statsQueryService.getFinancialStats(periodType, pageable));
    }

    @Operation(
            summary = "Get sales statistics",
            description =
                    "Fetch paginated sales aggregations (transaction counts, gross/net revenue, total discounts, and top-performing categories/products) filtered by period type.")
    @ApiResponse(responseCode = "200", description = "Sales statistics retrieved successfully")
    @GetMapping("/sales")
    public ResponseEntity<Page<SalesStatProjection>> getSalesStats(
            @RequestParam PeriodType periodType,
            @PageableDefault(sort = "statDate", direction = DESC) Pageable pageable) {

        return ResponseEntity.ok(statsQueryService.getSalesStats(periodType, pageable));
    }

    @Operation(
            summary = "Get employee performance leaderboard",
            description =
                    "Fetch paginated employee performance metrics (sales volume, total transactions, commissions earned). By default, this endpoint sorts by grossSalesAmount DESC to act as an instant leaderboard.")
    @ApiResponse(responseCode = "200", description = "Employee performance statistics retrieved successfully")
    @GetMapping("/employees")
    public ResponseEntity<Page<EmployeePerformanceStatProjection>> getEmployeePerformanceStats(
            @RequestParam PeriodType periodType,
            @PageableDefault(sort = "grossSalesAmount", direction = DESC) Pageable pageable) {

        return ResponseEntity.ok(statsQueryService.getEmployeePerformanceStats(periodType, pageable));
    }

    @Operation(
            summary = "Get stock snapshot statistics",
            description =
                    "Fetch a paginated snapshot of stock levels, values, and 30-day velocity for a specific historical date. By default, this sorts by quantityOnHand ASC to immediately highlight low-stock items.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Stock statistics retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid date format or period type")
    })
    @GetMapping("/stock")
    public ResponseEntity<Page<StockStatProjection>> getStockStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate statDate,
            @RequestParam PeriodType periodType,
            @PageableDefault(sort = "quantityOnHand", direction = ASC) Pageable pageable) {

        return ResponseEntity.ok(statsQueryService.getStockStats(statDate, periodType, pageable));
    }

    @Operation(
            summary = "Trigger stats calculation",
            description =
                    "Manually trigger statistics calculation for a given period type and date range. "
                            + "DAILY/WEEKLY compute sales, stock, and employee stats. MONTHLY/YEARLY also include financial stats.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Stats calculated and saved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid parameters")
    })
    @PreAuthorize("hasAuthority(@authorities.manageAuthoritiesAuthority)")
    @PostMapping("/calculate")
    public ResponseEntity<String> calculateStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam PeriodType periodType) {

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();

        salesStatsService.saveSalesStats(start, end, startDate, periodType);
        stockStatService.saveStockStats(startDate, periodType);
        employeePerformanceStatService.saveEmployeeStats(start, end, startDate, periodType);

        if (periodType == PeriodType.MONTHLY || periodType == PeriodType.YEARLY) {
            financialStatsService.saveFinancialStats(start, end, startDate, periodType);
        }

        return ResponseEntity.ok(
                "Stats calculated successfully for period %s from %s to %s.".formatted(periodType, startDate, endDate));
    }
}
