package com.sefault.server.prediction.scheduler;

import com.sefault.server.prediction.service.EmployeePredictionService;
import com.sefault.server.prediction.service.FinancialPredictionService;
import com.sefault.server.prediction.service.SalesPredictionService;
import com.sefault.server.prediction.service.StockPredictionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PredictionCronScheduler {

    private final SalesPredictionService salesPredictionService;
    private final StockPredictionService stockPredictionService;
    private final EmployeePredictionService employeePredictionService;
    private final FinancialPredictionService financialPredictionService;

    /**
     * Runs every day at 5:00 AM.
     * Generates daily forecasts for Sales, Stock, and Employees.
     */
    @Scheduled(cron = "0 0 5 * * *")
    public void runDailyPredictions() {
        log.info("⏰ [CRON 5:00 AM] Starting Daily Machine Learning Forecasts...");

        try {
            salesPredictionService.generateDailySalesForecast();
            stockPredictionService.generateStockForecasts();
            employeePredictionService.generateEmployeeForecasts();
            log.info("[CRON] Daily Forecasts completed successfully.");
        } catch (Exception e) {
            log.error("[CRON] Daily Forecasts failed: {}", e.getMessage(), e);
        }
    }

    /**
     * Runs at 6:00 AM on the 1st day of every month.
     * Generates the monthly financial outlook.
     */
    @Scheduled(cron = "0 0 6 1 * *")
    public void runMonthlyPredictions() {
        log.info("[CRON 6:00 AM] Starting Monthly Financial Forecast...");

        try {
            financialPredictionService.generateMonthlyFinancialForecast();
            log.info("[CRON] Monthly Financial Forecast completed successfully.");
        } catch (Exception e) {
            log.error("[CRON] Monthly Financial Forecast failed: {}", e.getMessage(), e);
        }
    }
}
