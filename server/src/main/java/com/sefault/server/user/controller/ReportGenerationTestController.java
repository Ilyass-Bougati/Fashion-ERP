package com.sefault.server.user.controller;

import com.sefault.server.stats.enums.PeriodType;
import com.sefault.server.user.dto.record.SavedReportRecord;
import com.sefault.server.user.enums.ReportCategory;
import com.sefault.server.user.service.ReportService;
import java.util.concurrent.Future;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("dev")
@RestController
@RequestMapping("/test/api/report")
@RequiredArgsConstructor
public class ReportGenerationTestController {
    private final ReportService reportService;

    @PostMapping
    public Future<SavedReportRecord> reportGen(@RequestBody ReportCategory category) {
        return reportService.generateReport(PeriodType.MONTHLY, category);
    }
}
