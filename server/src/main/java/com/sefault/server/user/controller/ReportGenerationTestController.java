package com.sefault.server.user.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sefault.server.stats.enums.PeriodType;
import com.sefault.server.user.enums.ReportType;
import com.sefault.server.user.service.impl.ReportServiceImpl;
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
    public void reportGen() throws JsonProcessingException {
        reportService.generateReport(PeriodType.MONTHLY, ReportType.PDF);
    }
}
