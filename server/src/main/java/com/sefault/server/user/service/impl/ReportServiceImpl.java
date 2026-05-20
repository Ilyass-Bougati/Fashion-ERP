package com.sefault.server.user.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sefault.server.ai.service.LlmService;
import com.sefault.server.exception.FailedReportGenerationException;
import com.sefault.server.exception.InvalidPeriodException;
import com.sefault.server.exception.NotFoundException;
import com.sefault.server.minio.MinioProperties;
import com.sefault.server.minio.MinioService;
import com.sefault.server.stats.dto.projection.EmployeePerformanceStatProjection;
import com.sefault.server.stats.dto.projection.FinancialStatProjection;
import com.sefault.server.stats.dto.projection.SalesStatProjection;
import com.sefault.server.stats.enums.PeriodType;
import com.sefault.server.stats.service.StatsQueryService;
import com.sefault.server.user.dto.record.ReportRecord;
import com.sefault.server.user.dto.record.SavedReportRecord;
import com.sefault.server.user.entity.Report;
import com.sefault.server.user.enums.ReportCategory;
import com.sefault.server.user.enums.ReportStatus;
import com.sefault.server.user.enums.ReportType;
import com.sefault.server.user.mapper.ReportMapper;
import com.sefault.server.user.repository.ReportRepository;
import com.sefault.server.user.service.PdfGenerationService;
import com.sefault.server.user.service.ReportService;
import io.minio.errors.MinioException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {
    private final StatsQueryService statsQueryService;
    private final PdfGenerationService pdfGenerationService;
    private final MinioService minioService;
    private final MinioProperties minioProperties;
    private final ReportRepository reportRepository;
    private final ReportMapper reportMapper;
    private final LlmService llmService;
    private final ObjectMapper objectMapper;

    private String getInsight(String moduleName, String data) {
        String prompt = "ERP Module: " + moduleName + "\n" + "Data: "
                + data + "\n" + "\n"
                + "Write an insight paragraph for a business report based on this data.";

        return llmService.prompt(prompt);
    }

    private void uploadFile(byte[] fileBytes, String fileName) throws MinioException, IOException {
        if (fileBytes == null) throw new IOException();
        InputStream file = new ByteArrayInputStream(fileBytes);
        minioService.uploadFile(minioProperties.reportsBucket(), fileName, file, fileBytes.length, "application/pdf");
    }

    @Async
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CompletableFuture<SavedReportRecord> generateReport(PeriodType period, ReportCategory category)
            throws FailedReportGenerationException {
        String title = period + "-" + category + "-" + LocalDateTime.now();
        ReportRecord saved = save(new ReportRecord(
                null,
                title,
                ReportType.PDF,
                ReportStatus.PENDING,
                title,
                minioProperties.reportsBucket(),
                "application/pdf",
                category,
                null));

        PeriodType newPeriod;
        LocalDate now = LocalDate.now(), date1 = null, date2 = null;

        switch (period) {
            case MONTHLY:
                newPeriod = PeriodType.DAILY;
                date1 = now.with(TemporalAdjusters.firstDayOfMonth());
                date2 = now.with(TemporalAdjusters.lastDayOfMonth());
                break;

            case YEARLY:
                newPeriod = PeriodType.MONTHLY;
                date1 = now.with(TemporalAdjusters.firstDayOfYear());
                date2 = now.with(TemporalAdjusters.lastDayOfYear());
                break;

            default:
                newPeriod = null;
        }

        if (newPeriod == null) {
            throw new InvalidPeriodException("Given period" + period + " is invalid !");
        }

        try {
            byte[] fileBytes;
            switch (category) {
                case FINANCIAL:
                    List<FinancialStatProjection> financialData =
                            statsQueryService.getAllFinancialStats(newPeriod, date1, date2);
                    String financeInsight = getInsight("finance", objectMapper.writeValueAsString(financialData));
                    fileBytes = pdfGenerationService.generateDocument(
                            "reports/financial-report.html",
                            Map.of("stats", financialData, "aiInsight", financeInsight));
                    break;

                case SALES:
                    List<SalesStatProjection> salesData = statsQueryService.getAllSalesStats(newPeriod, date1, date2);
                    String salesInsight = getInsight("sales", objectMapper.writeValueAsString(salesData));
                    fileBytes = pdfGenerationService.generateDocument(
                            "reports/sales-report.html", Map.of("stats", salesData, "aiInsight", salesInsight));
                    break;
                case EMPLOYEE_PERFORMANCE:
                    List<EmployeePerformanceStatProjection> employeePerformanceData =
                            statsQueryService.getAllEmployeePerformanceStats(newPeriod, date1, date2);
                    String employeePerformanceInsight = getInsight(
                            "employee performance", objectMapper.writeValueAsString(employeePerformanceData));
                    fileBytes = pdfGenerationService.generateDocument(
                            "reports/employee-performance-report.html",
                            Map.of("stats", employeePerformanceData, "aiInsight", employeePerformanceInsight));
                    break;
                default:
                    fileBytes = null;
            }
            uploadFile(fileBytes, title);
        } catch (Exception e) {
            save(new ReportRecord(
                    saved.id(),
                    title,
                    ReportType.PDF,
                    ReportStatus.FAILED,
                    title,
                    minioProperties.reportsBucket(),
                    "application/pdf",
                    category,
                    null));
            throw new FailedReportGenerationException(e.getMessage());
        }

        save(new ReportRecord(
                saved.id(),
                title,
                ReportType.PDF,
                ReportStatus.DONE,
                title,
                minioProperties.reportsBucket(),
                "application/pdf",
                category,
                null));
        return CompletableFuture.completedFuture(new SavedReportRecord(
                saved.id(), minioService.getPermanentFileUrl(minioProperties.reportsBucket(), title), null));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReportRecord> getAll(Pageable pageable) {
        return reportRepository.findAllBy(pageable).map(reportMapper::projectionToRecord);
    }

    @Transactional(readOnly = true)
    public SavedReportRecord getUrlById(UUID id) throws MinioException {
        return new SavedReportRecord(
                id,
                minioService.getFileUrl(
                        minioProperties.reportsBucket(), getById(id).objectKey(), minioProperties.expirationDuration()),
                LocalDateTime.now().plusSeconds(minioProperties.expirationDuration()));
    }

    @Override
    @Transactional(readOnly = true)
    public ReportRecord getById(UUID id) {
        return reportRepository
                .getReportProjectionById(id)
                .map(reportMapper::projectionToRecord)
                .orElseThrow(() -> new NotFoundException("Report not found with id : " + id));
    }

    private ReportRecord save(ReportRecord record) {
        Report entity = reportMapper.toEntity(record);
        Report saved = reportRepository.save(entity);
        return reportMapper.entityToRecord(saved);
    }

    @Override
    public void deleteById(UUID id) throws MinioException {
        ReportRecord record = getById(id);
        minioService.deleteFile(minioProperties.reportsBucket(), record.objectKey());
        reportRepository.deleteById(id);
    }
}
