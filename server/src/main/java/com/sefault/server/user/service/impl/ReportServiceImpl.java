package com.sefault.server.user.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sefault.server.ai.service.LlmService;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReportServiceImpl implements ReportService {
    private final StatsQueryService statsQueryService;
    private final PdfGenerationService pdfGenerationService;
    private final MinioService minioService;
    private final MinioProperties minioProperties;
    private final ReportRepository reportRepository;
    private final ReportMapper reportMapper;
    private final LlmService llmService;
    private final ObjectMapper objectMapper;

    public ReportServiceImpl(
            StatsQueryService statsQueryService,
            PdfGenerationService pdfGenerationService,
            MinioService minioService,
            MinioProperties minioProperties,
            ReportRepository reportRepository,
            ReportMapper reportMapper,
            LlmService llmService,
            ObjectMapper objectMapper) {
        this.statsQueryService = statsQueryService;
        this.pdfGenerationService = pdfGenerationService;
        this.minioService = minioService;
        this.minioProperties = minioProperties;
        this.reportRepository = reportRepository;
        this.reportMapper = reportMapper;
        this.llmService = llmService;
        this.objectMapper = objectMapper;
        this.minioService.setBucketName(minioProperties.reportsBucket());
    }

    private String getInsight(String moduleName, String data) {
        String prompt = "ERP Module: " + moduleName + "\n" + "Data: "
                + data + "\n" + "\n"
                + "Write an insight paragraph for a business report based on this data.";

        return llmService.prompt(prompt);
    }

    private void uploadFile(byte[] fileBytes, String fileName) throws MinioException, IOException {
        InputStream file = new ByteArrayInputStream(fileBytes);
        minioService.uploadFile(fileName, file, fileBytes.length, "application/pdf");
    }

    public SavedReportRecord generateReport(PeriodType period) throws IOException, MinioException {
        List<FinancialStatProjection> financialData = statsQueryService.getAllFinancialStats(period);
        List<SalesStatProjection> salesData = statsQueryService.getAllSalesStats(period);
        List<EmployeePerformanceStatProjection> employeePerformanceData =
                statsQueryService.getAllEmployeePerformanceStats(period);

        String financeInsight = getInsight("finance", objectMapper.writeValueAsString(financialData));
        String salesInsight = getInsight("sales", objectMapper.writeValueAsString(salesData));
        String employeePerformanceInsight =
                getInsight("employee performance", objectMapper.writeValueAsString(employeePerformanceData));

        byte[] fileBytes = pdfGenerationService.generateDocument(
                "reports/combined-report",
                Map.of(
                        "fStats",
                        financialData,
                        "fInsight",
                        financeInsight,
                        "sStats",
                        salesData,
                        "sInsight",
                        salesInsight,
                        "eStats",
                        employeePerformanceData,
                        "eInsight",
                        employeePerformanceInsight));
        String fileName = UUID.randomUUID().toString();
        uploadFile(fileBytes, fileName);
        ReportRecord saved = save(new ReportRecord(
                null,
                fileName,
                ReportType.PDF,
                ReportStatus.DONE,
                fileName,
                minioProperties.reportsBucket(),
                "application/pdf",
                null,
                null));

        return new SavedReportRecord(saved.id(), minioService.getPermanentFileUrl(fileName));
    }

    @Override
    public Page<ReportRecord> getAll(Pageable pageable) {
        return reportRepository.findAllBy(pageable).map(reportMapper::projectionToRecord);
    }

    @Override
    public ReportRecord getById(UUID id) {
        return reportRepository
                .getReportProjectionById(id)
                .map(reportMapper::projectionToRecord)
                .orElseThrow(() -> new NotFoundException("Report not found with id : " + id));
    }

    @Override
    public ReportRecord save(ReportRecord record) {
        Report entity = reportMapper.toEntity(record);
        Report saved = reportRepository.save(entity);
        return reportMapper.entityToRecord(saved);
    }

    @Override
    public void deleteById(UUID id) {
        if (!reportRepository.existsById(id)) {
            throw new NotFoundException("Report not found by id : " + id);
        } else {
            reportRepository.deleteById(id);
        }
    }
}
