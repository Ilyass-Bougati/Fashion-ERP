package com.sefault.server.user.dto.projection;

import com.sefault.server.user.entity.ReportCategory;
import com.sefault.server.user.entity.UserReport;
import com.sefault.server.user.enums.ReportStatus;
import com.sefault.server.user.enums.ReportType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface ReportProjection {
    UUID getId();

    String getTitle();

    ReportType getType();

    ReportStatus getStatus();

    String getObjectKey();

    String getBucketName();

    String getContentType();

    List<UserReport> getUserReports();

    Set<ReportCategory> getCategories();

    LocalDateTime getGeneratedAt();
}
