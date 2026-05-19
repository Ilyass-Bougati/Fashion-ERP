package com.sefault.server.user.service;

import com.sefault.server.user.dto.record.UserReportRecord;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserReportService {
    Page<UserReportRecord> getAll(Pageable pageable);

    void logAccess(String email, UUID reportId);
}
