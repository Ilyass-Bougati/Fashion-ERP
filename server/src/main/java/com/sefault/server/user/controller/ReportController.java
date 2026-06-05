package com.sefault.server.user.controller;

import com.sefault.server.user.dto.record.ReportRecord;
import com.sefault.server.user.dto.record.SavedReportRecord;
import com.sefault.server.user.service.ReportService;
import com.sefault.server.user.service.UserReportService;
import io.minio.errors.MinioException;
import java.security.Principal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority(@authorities.readReportsAuthority)")
public class ReportController {
    private final ReportService reportService;
    private final UserReportService userReportService;

    @GetMapping
    public Page<ReportRecord> getAll(Pageable pageable) {
        return reportService.getAll(pageable);
    }

    @GetMapping("/{id}")
    public SavedReportRecord getById(Principal principal, @PathVariable UUID id) throws MinioException {
        try {
            userReportService.logAccess(principal.getName(), id);
        } catch (Exception e) {
            log.warn("Failed to log report access for user {} on report {}: {}", principal.getName(), id, e.getMessage());
        }
        return reportService.getUrlById(id);
    }
}
