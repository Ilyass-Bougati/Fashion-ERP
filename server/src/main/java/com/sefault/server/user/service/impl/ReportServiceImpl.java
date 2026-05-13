package com.sefault.server.user.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import com.sefault.server.user.enums.ReportType;
import com.sefault.server.user.mapper.ReportMapper;
import com.sefault.server.user.repository.ReportRepository;
import com.sefault.server.user.service.PdfGenerationService;
import com.sefault.server.user.service.ReportService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

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
            StatsQueryService statsQueryService, PdfGenerationService pdfGenerationService, MinioService minioService,
            MinioProperties minioProperties,
            ReportRepository reportRepository,
            ReportMapper reportMapper,
            LlmService llmService, ObjectMapper objectMapper) {
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

    private String getInsight(String moduleName, String data){
        String prompt = "ERP Module: " + moduleName+ "\n" +
                "Data: "+ data +"\n" +
                "\n" +
                "Write an insight paragraph for a business report based on this data.";

        System.out.println(prompt);

        return llmService.prompt(prompt);
    }

    public SavedReportRecord generateReport(PeriodType period , ReportType type) throws JsonProcessingException {
        //Get all necessary stats
        List<FinancialStatProjection> financialData = statsQueryService.getAllFinancialStats(period);
        List<SalesStatProjection> salesData = statsQueryService.getAllSalesStats(period);
        List<EmployeePerformanceStatProjection> employeePerformanceData = statsQueryService.getAllEmployeePerformanceStats(period);

        //Send to LLM
        String financeInsight = getInsight("finance", objectMapper.writeValueAsString(financialData));
//        String salesInsight = getInsight("sales", salesData.toString());
//        String employeePerformanceInsight = getInsight("employee performance", financialData.toString());

        System.out.println(financeInsight);
//        System.out.println(salesInsight);
//        System.out.println(employeePerformanceInsight);

        //Compile template
        String html = pdfGenerationService.processTemplate("reports/test-report", Map.of("stats", financialData, "aiInsight", financeInsight));
        System.out.println(html);
        //Render template into document
        return new SavedReportRecord(UUID.randomUUID(), "");
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
