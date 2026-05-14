package com.sefault.server.user.controller;

import com.sefault.server.stats.enums.PeriodType;
import com.sefault.server.user.dto.record.SavedReportRecord;
import com.sefault.server.user.service.impl.ReportServiceImpl;
import io.minio.errors.MinioException;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test/api/report")
@RequiredArgsConstructor
public class ReportGenerationTestController {
    private final ReportServiceImpl reportService;

    @GetMapping
    public SavedReportRecord reportGen() throws IOException, MinioException {
        return reportService.generateReport(PeriodType.MONTHLY);
    }
}
