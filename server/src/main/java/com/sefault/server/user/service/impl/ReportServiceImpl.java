package com.sefault.server.user.service.impl;

import com.sefault.server.ai.service.LlmService;
import com.sefault.server.exception.NotFoundException;
import com.sefault.server.minio.MinioProperties;
import com.sefault.server.minio.MinioService;
import com.sefault.server.user.dto.record.ReportRecord;
import com.sefault.server.user.entity.Report;
import com.sefault.server.user.mapper.ReportMapper;
import com.sefault.server.user.repository.ReportRepository;
import com.sefault.server.user.service.ReportService;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReportServiceImpl implements ReportService {
    private final MinioService minioService;
    private final MinioProperties minioProperties;
    private final ReportRepository reportRepository;
    private final ReportMapper reportMapper;
    private final LlmService llmService;

    public ReportServiceImpl(
            MinioService minioService,
            MinioProperties minioProperties,
            ReportRepository reportRepository,
            ReportMapper reportMapper,
            LlmService llmService) {
        this.minioService = minioService;
        this.minioProperties = minioProperties;
        this.reportRepository = reportRepository;
        this.reportMapper = reportMapper;
        this.llmService = llmService;
        this.minioService.setBucketName(minioProperties.reportsBucket());
    }

    // Report generation & persistence on minIO

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
