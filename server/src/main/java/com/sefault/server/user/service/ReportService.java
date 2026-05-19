package com.sefault.server.user.service;

import com.sefault.server.exception.FailedReportGenerationException;
import com.sefault.server.stats.enums.PeriodType;
import com.sefault.server.user.dto.record.ReportRecord;
import com.sefault.server.user.dto.record.SavedReportRecord;
import com.sefault.server.user.enums.ReportCategory;
import io.minio.errors.MinioException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReportService {
    Page<ReportRecord> getAll(Pageable pageable);

    SavedReportRecord getUrlById(UUID id) throws MinioException;

    ReportRecord getById(UUID id);

    CompletableFuture<SavedReportRecord> generateReport(PeriodType period, ReportCategory category)
            throws FailedReportGenerationException;

    void deleteById(UUID id) throws MinioException;
}
