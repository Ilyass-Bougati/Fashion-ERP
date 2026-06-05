package com.sefault.server.user.controller;

import com.sefault.server.minio.MinioService;
import com.sefault.server.user.dto.record.ReportRecord;
import com.sefault.server.user.service.ReportService;
import com.sefault.server.user.service.UserReportService;
import io.minio.errors.MinioException;
import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@Slf4j
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority(@authorities.readReportsAuthority)")
public class ReportController {
    private final ReportService reportService;
    private final UserReportService userReportService;
    private final MinioService minioService;

    @GetMapping
    public Page<ReportRecord> getAll(Pageable pageable) {
        return reportService.getAll(pageable);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<StreamingResponseBody> download(Principal principal, @PathVariable UUID id)
            throws MinioException, IOException {
        try {
            userReportService.logAccess(principal.getName(), id);
        } catch (Exception e) {
            log.warn("Failed to log report access for user {} on report {}: {}", principal.getName(), id, e.getMessage());
        }

        ReportRecord report = reportService.getById(id);
        InputStream stream = minioService.getObject(report.bucketName(), report.objectKey());

        StreamingResponseBody body = out -> {
            try (stream) { stream.transferTo(out); }
        };

        String filename = report.title() + "." + report.type().name().toLowerCase();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(body);
    }
}
