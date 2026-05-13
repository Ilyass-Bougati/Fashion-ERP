package com.sefault.server.prediction.controller;

import com.sefault.server.prediction.dto.record.*;
import com.sefault.server.prediction.service.PredictionQueryService;
import com.sefault.server.stats.enums.PeriodType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/predictions")
@RequiredArgsConstructor
@Tag(
        name = "Machine Learning Predictions",
        description = "Read-only endpoints for retrieving AI-generated business forecasts (powered by TimesFM).")
public class PredictionController {

    private final PredictionQueryService predictionQueryService;

    @Operation(
            summary = "Get Store Sales Forecasts",
            description =
                    "Retrieves predicted daily store performance metrics, including expected Net Revenue, Units Sold, and Total Transactions. Each metric includes a 10th-90th percentile confidence interval (Lower and Upper bounds).")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved a paginated list of sales predictions"),
        @ApiResponse(
                responseCode = "400",
                description = "Invalid date format or missing parameters",
                content = @Content)
    })
    @GetMapping("/sales")
    public ResponseEntity<Page<SalesPredictionRecord>> getSalesPredictions(
            @Parameter(
                            description =
                                    "Fetch predictions starting from this date (inclusive). Usually set to today's date.",
                            example = "2026-05-12")
                    @RequestParam
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate fromDate,
            @Parameter(description = "The aggregation period. Expected value is DAILY.", example = "DAILY")
                    @RequestParam
                    PeriodType periodType,
            @Parameter(
                            description =
                                    "Pagination parameters (page number, size). Default size is 30 (1 month forecast).")
                    @PageableDefault(size = 30)
                    Pageable pageable) {

        return ResponseEntity.ok(predictionQueryService.getSalesPredictions(fromDate, periodType, pageable));
    }

    @Operation(
            summary = "Get Inventory/Stock Forecasts by SKU",
            description =
                    "Retrieves the predicted depletion of stock levels for a specific product variation over the coming days. Critical for automated reordering and avoiding stockouts.")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved stock predictions for the requested SKU"),
        @ApiResponse(
                responseCode = "400",
                description = "Invalid date format, missing parameters, or missing SKU",
                content = @Content)
    })
    @GetMapping("/stock")
    public ResponseEntity<Page<StockPredictionRecord>> getStockPredictions(
            @Parameter(description = "Fetch predictions starting from this date (inclusive).", example = "2026-05-12")
                    @RequestParam
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate fromDate,
            @Parameter(description = "The aggregation period. Expected value is DAILY.", example = "DAILY")
                    @RequestParam
                    PeriodType periodType,
            @Parameter(description = "The exact Product Variation SKU to forecast.", example = "SKU-SEED-TEST")
                    @RequestParam
                    String sku,
            @Parameter(description = "Pagination parameters. Default size is 30 (1 month forecast).")
                    @PageableDefault(size = 30)
                    Pageable pageable) {

        return ResponseEntity.ok(predictionQueryService.getStockPredictions(fromDate, periodType, sku, pageable));
    }

    @Operation(
            summary = "Get Macro Financial Forecasts",
            description =
                    "Retrieves high-level quarterly/annual predictions for Total Corporate Revenue and Net Profit.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved financial predictions"),
        @ApiResponse(responseCode = "400", description = "Invalid parameters", content = @Content)
    })
    @GetMapping("/financial")
    public ResponseEntity<Page<FinancialPredictionRecord>> getFinancialPredictions(
            @Parameter(
                            description =
                                    "Fetch predictions starting from this date. Usually the 1st of the current month.",
                            example = "2026-05-01")
                    @RequestParam
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate fromDate,
            @Parameter(description = "The aggregation period. Expected value is MONTHLY.", example = "MONTHLY")
                    @RequestParam
                    PeriodType periodType,
            @Parameter(description = "Pagination parameters. Default size is 12 (1 year forecast).")
                    @PageableDefault(size = 12)
                    Pageable pageable) {

        return ResponseEntity.ok(predictionQueryService.getFinancialPredictions(fromDate, periodType, pageable));
    }

    @Operation(
            summary = "Get Employee Performance Forecasts",
            description =
                    "Retrieves the expected Gross Sales volume generated by a specific employee. Used for setting quotas and estimating future commissions.")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved performance predictions for the employee"),
        @ApiResponse(responseCode = "400", description = "Invalid parameters or missing CIN", content = @Content)
    })
    @GetMapping("/employees")
    public ResponseEntity<Page<EmployeePerformancePredictionRecord>> getEmployeePredictions(
            @Parameter(description = "Fetch predictions starting from this date.", example = "2026-05-12")
                    @RequestParam
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate fromDate,
            @Parameter(description = "The aggregation period. Expected value is DAILY.", example = "DAILY")
                    @RequestParam
                    PeriodType periodType,
            @Parameter(
                            description = "The unique Carte d'Identité Nationale (CIN) of the employee.",
                            example = "CIN-SEED-EMP")
                    @RequestParam
                    String cin,
            @Parameter(description = "Pagination parameters. Default size is 30.") @PageableDefault(size = 30)
                    Pageable pageable) {

        return ResponseEntity.ok(predictionQueryService.getEmployeePredictions(fromDate, periodType, cin, pageable));
    }
}
