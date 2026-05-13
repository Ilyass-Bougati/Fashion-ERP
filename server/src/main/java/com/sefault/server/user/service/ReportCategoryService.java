package com.sefault.server.user.service;

import com.sefault.server.user.dto.record.ReportCategoryRecord;
import java.util.List;
import java.util.UUID;

public interface ReportCategoryService {
    List<ReportCategoryRecord> getAll();

    ReportCategoryRecord getById(UUID id);

    ReportCategoryRecord save(ReportCategoryRecord record);

    ReportCategoryRecord update(UUID id, ReportCategoryRecord record);

    void deleteById(UUID id);
}
