package com.sefault.server.user.dto.projection;

import com.sefault.server.user.enums.ReportCategory;
import com.sefault.server.user.enums.ReportStatus;
import com.sefault.server.user.enums.ReportType;
import java.time.LocalDateTime;
import java.util.UUID;

public interface ReportProjection {
    UUID getId();

    String getTitle();

    ReportType getType();

    ReportStatus getStatus();

    String getObjectKey();

    String getBucketName();

    String getContentType();

    ReportCategory getCategory();

    LocalDateTime getGeneratedAt();
}
