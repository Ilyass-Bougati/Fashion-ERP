package com.sefault.server.stats.controller;

import com.sefault.server.stats.enums.PeriodType;
import com.sefault.server.stats.scheduler.StatsCronScheduler;
import com.sefault.server.stats.service.impl.EmployeePerformanceStatServiceImpl;
import com.sefault.server.stats.service.impl.FinancialStatsServiceImpl;
import com.sefault.server.stats.service.impl.SalesStatsServiceImpl;
import com.sefault.server.stats.service.impl.StockStatServiceImpl;
import com.sefault.server.stats.service.test.DatabaseSeederService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

@RestController
@RequestMapping("/api/v1/test/stats")
@RequiredArgsConstructor
public class TestStatsController {

    private final FinancialStatsServiceImpl financialService;
    private final SalesStatsServiceImpl salesService;
    private final StockStatServiceImpl stockService;
    private final EmployeePerformanceStatServiceImpl employeeService;
    private final DatabaseSeederService seederService;
    private final StatsCronScheduler cronScheduler;

    @PostMapping("/run-current-month")
    public ResponseEntity<String> runCurrentMonthStats() {

        YearMonth currentMonth = YearMonth.now();
        LocalDate firstDay = currentMonth.atDay(1);
        LocalDateTime startOfMonth = firstDay.atStartOfDay();
        LocalDateTime endOfMonth = currentMonth.plusMonths(1).atDay(1).atStartOfDay();

        financialService.saveFinancialStats(startOfMonth, endOfMonth, firstDay, PeriodType.MONTHLY);

        salesService.saveSalesStats(startOfMonth, endOfMonth, firstDay, PeriodType.MONTHLY);

        stockService.saveStockStats(LocalDate.now(), PeriodType.DAILY);

        employeeService.saveEmployeeStats(startOfMonth, endOfMonth, firstDay, PeriodType.MONTHLY);

        return ResponseEntity.ok("All stats successfully calculated and saved to the database. Check your tables!");
    }

    @PostMapping("/seed-database")
    public ResponseEntity<String> seedDatabase() {
        seederService.seedDatabase();
        return ResponseEntity.ok("Database seeded successfully!");
    }

    @PostMapping("/trigger-daily-cron")
    public ResponseEntity<String> triggerDailyCron() {
        cronScheduler.runDailyReconciliation();
        return ResponseEntity.ok("Manual trigger sent to Daily Cron Job. Check logs!");
    }

    @PostMapping("/trigger-Weekly-cron")
    public ResponseEntity<String> triggerWeeklyCron() {
        cronScheduler.runWeeklyReconciliation();
        return ResponseEntity.ok("Manual trigger sent to Weekly Cron Job. Check logs!");
    }

    @PostMapping("/trigger-monthly-cron")
    public ResponseEntity<String> triggerMonthlyCron() {
        cronScheduler.runMonthlyReconciliation();
        return ResponseEntity.ok("Manual trigger sent to Monthly Cron Job. Check logs!");
    }
}