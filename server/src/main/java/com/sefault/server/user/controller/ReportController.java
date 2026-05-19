package com.sefault.server.user.controller;

import com.sefault.server.user.dto.record.ReportRecord;
import com.sefault.server.user.dto.record.SavedReportRecord;
import com.sefault.server.user.service.ReportService;

import java.security.Principal;
import java.util.UUID;

import com.sefault.server.user.service.UserReportService;
import io.minio.errors.MinioException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;
    private final UserReportService userReportService;

    @GetMapping
    public Page<ReportRecord> getAll(Pageable pageable) {
        return reportService.getAll(pageable);
    }

    @GetMapping("/{id}")
    public SavedReportRecord getById(Principal principal, @PathVariable UUID id) throws MinioException {
        userReportService.logAccess(principal.getName(), id);
        return reportService.getUrlById(id);
    }
}
