package com.sefault.server.user.dto.record;

import com.sefault.server.user.entity.ReportCategory;
import com.sefault.server.user.entity.UserReport;
import com.sefault.server.user.enums.ReportStatus;
import com.sefault.server.user.enums.ReportType;

import java.time.LocalDateTime;
import java.util.List;
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
        List<UserReport> userReports,
        Set<ReportCategory> categories,
        LocalDateTime generatedAt
) {
}

