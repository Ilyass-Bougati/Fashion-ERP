package com.sefault.server.user.scheduler;

import com.sefault.server.exception.FailedReportGenerationException;
import com.sefault.server.stats.enums.PeriodType;
import com.sefault.server.user.enums.ReportCategory;
import com.sefault.server.user.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReportCronScheduler {
    private final ReportService reportService;

    @Scheduled(cron = "0 0 0 1 * *")
    private void generateMonthlyReports(){
        try{
            reportService.generateReport(PeriodType.MONTHLY, ReportCategory.FINANCIAL);
            log.info("Monthly financial report successfully generated !");
        }catch(FailedReportGenerationException e){
            log.error("Monthly financial report generation failed.");
        }
        try{
            reportService.generateReport(PeriodType.MONTHLY, ReportCategory.SALES);
            log.info("Monthly sales report successfully generated !");
        }catch (FailedReportGenerationException e){
            log.error("Monthly sales report generation failed.");
        }
        try{
            reportService.generateReport(PeriodType.MONTHLY, ReportCategory.EMPLOYEE_PERFORMANCE);
            log.info("Monthly employee performance report successfully generated !");
        } catch (FailedReportGenerationException e) {
            log.error("Monthly employee performance report generation failed.");
        }
    }

    @Scheduled(cron = "0 0 0 1 1 *")
    private void generateYearlyReports(){
        try{
            reportService.generateReport(PeriodType.YEARLY, ReportCategory.FINANCIAL);
            log.info("Yearly financial report successfully generated !");
        }catch(FailedReportGenerationException e){
            log.error("Yearly financial report generation failed.");
        }
        try{
            reportService.generateReport(PeriodType.YEARLY, ReportCategory.SALES);
            log.info("Yearly sales report successfully generated !");
        }catch (FailedReportGenerationException e){
            log.error("Yearly sales report generation failed.");
        }
        try{
            reportService.generateReport(PeriodType.YEARLY, ReportCategory.EMPLOYEE_PERFORMANCE);
            log.info("Yearly employee performance report successfully generated !");
        } catch (FailedReportGenerationException e) {
            log.error("Yearly employee performance report generation failed.");
        }
    }
}
