package com.sefault.server.user.dto.record;

import com.sefault.server.user.enums.ReportStatus;
import com.sefault.server.user.enums.ReportType;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public record ReportRecord(
        UUID id,
        String title,
        ReportType type,
        ReportStatus status,
        String objectKey,
        String bucketName,
        String contentType,
        Set<ReportCategoryRecord> categories,
        LocalDateTime generatedAt) {}
