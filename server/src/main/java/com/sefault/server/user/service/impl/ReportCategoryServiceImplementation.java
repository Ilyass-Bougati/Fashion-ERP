package com.sefault.server.user.service.impl;

import com.sefault.server.exception.NotFoundException;
import com.sefault.server.user.dto.record.ReportCategoryRecord;
import com.sefault.server.user.entity.ReportCategory;
import com.sefault.server.user.mapper.ReportCategoryMapper;
import com.sefault.server.user.repository.ReportCategoryRepository;
import com.sefault.server.user.service.ReportCategoryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportCategoryServiceImplementation implements ReportCategoryService {
    private final ReportCategoryMapper reportCategoryMapper;
    private final ReportCategoryRepository reportCategoryRepository;

    @Transactional(readOnly = true)
    @Override
    public List<ReportCategoryRecord> getAll() {
        return reportCategoryRepository.findAllBy().stream()
                .map(reportCategoryMapper::projectionToRecord)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public ReportCategoryRecord getById(UUID id) {
        return reportCategoryRepository
                .getReportCategoryProjectionById(id)
                .map(reportCategoryMapper::projectionToRecord)
                .orElseThrow(() -> new NotFoundException("Report category not found with id : " + id));
    }

    @Override
    public ReportCategoryRecord save(ReportCategoryRecord record) {
        ReportCategory entity = reportCategoryMapper.toEntity(record);
        ReportCategory saved = reportCategoryRepository.save(entity);
        return reportCategoryMapper.entityToRecord(saved);
    }

    @Override
    public ReportCategoryRecord update(UUID id, ReportCategoryRecord record) {
        ReportCategory entity = findOrThrow(id);
        reportCategoryMapper.updateEntityFromRecord(record, entity);
        return reportCategoryMapper.entityToRecord(reportCategoryRepository.save(entity));
    }

    @Override
    public void deleteById(UUID id) {
        if (!reportCategoryRepository.existsById(id)) {
            throw new NotFoundException("Report category not found by id : " + id);
        } else {
            reportCategoryRepository.deleteById(id);
        }
    }

    private ReportCategory findOrThrow(UUID id) throws NotFoundException {
        return reportCategoryRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException("Report category not found with id : " + id));
    }
}
