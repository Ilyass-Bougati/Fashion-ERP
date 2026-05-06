package com.sefault.server.stats.scheduler;

import com.sefault.server.stats.enums.PeriodType;
import com.sefault.server.stats.service.impl.EmployeePerformanceStatServiceImpl;
import com.sefault.server.stats.service.impl.FinancialStatsServiceImpl;
import com.sefault.server.stats.service.impl.SalesStatsServiceImpl;
import com.sefault.server.stats.service.impl.StockStatServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;

@Slf4j
@Component
@RequiredArgsConstructor
public class StatsCronScheduler {
    private final FinancialStatsServiceImpl financialService;
    private final SalesStatsServiceImpl salesService;
    private final StockStatServiceImpl stockService;
    private final EmployeePerformanceStatServiceImpl employeeService;

    @Scheduled(cron = "0 0 2 * * *")
    public void runDailyReconciliation() {
        log.info("Starting Daily Stats Reconciliation...");

        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDateTime startOfDay = yesterday.atStartOfDay();
        LocalDateTime endOfDay = yesterday.plusDays(1).atStartOfDay();

        try {
            salesService.saveSalesStats(startOfDay, endOfDay, yesterday, PeriodType.DAILY);
            stockService.saveStockStats(yesterday, PeriodType.DAILY);
            employeeService.saveEmployeeStats(startOfDay, endOfDay, yesterday, PeriodType.DAILY);
            log.info("Daily Stats Reconciliation Complete.");
        } catch (Exception e) {
            log.error("Failed to process daily stats for {}: {}", yesterday, e.getMessage(), e);
        }
    }

    @Scheduled(cron = "0 0 3 * * MON")
    public void runWeeklyReconciliation() {
        log.info("Starting Weekly Stats Reconciliation...");

        LocalDate lastMonday = LocalDate.now().minusWeeks(1).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDateTime startOfWeek = lastMonday.atStartOfDay();
        LocalDateTime endOfWeek = lastMonday.plusWeeks(1).atStartOfDay();

        try {
            salesService.saveSalesStats(startOfWeek, endOfWeek, lastMonday, PeriodType.WEEKLY);
            employeeService.saveEmployeeStats(startOfWeek, endOfWeek, lastMonday, PeriodType.WEEKLY);
            log.info("Weekly Stats Reconciliation Complete.");
        } catch (Exception e) {
            log.error("Failed to process weekly stats for week starting {}: {}", lastMonday, e.getMessage(), e);
        }
    }

    @Scheduled(cron = "0 0 4 1 * *")
    public void runMonthlyReconciliation() {
        log.info("Starting Monthly Stats Reconciliation...");

        YearMonth lastMonth = YearMonth.now().minusMonths(1);
        LocalDate firstDayOfLastMonth = lastMonth.atDay(1);
        LocalDateTime startOfMonth = firstDayOfLastMonth.atStartOfDay();
        LocalDateTime endOfMonth = lastMonth.plusMonths(1).atDay(1).atStartOfDay();

        try {
            financialService.saveFinancialStats(startOfMonth, endOfMonth, firstDayOfLastMonth, PeriodType.MONTHLY);
            salesService.saveSalesStats(startOfMonth, endOfMonth, firstDayOfLastMonth, PeriodType.MONTHLY);
            employeeService.saveEmployeeStats(startOfMonth, endOfMonth, firstDayOfLastMonth, PeriodType.MONTHLY);
            log.info("Monthly Stats Reconciliation Complete.");
        } catch (Exception e) {
            log.error("Failed to process monthly stats for month starting {}: {}", firstDayOfLastMonth, e.getMessage(), e);
        }
    }
}
