package com.sefault.server.user.service;

import com.sefault.server.user.dto.record.ReportRecord;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReportService {
    Page<ReportRecord> getAll(Pageable pageable);

    ReportRecord getById(UUID id);

    ReportRecord save(ReportRecord record);

    void deleteById(UUID id);
}
