package com.sefault.server.user.dto.projection;

import com.sefault.server.user.entity.Report;
import java.util.Set;
import java.util.UUID;

public interface ReportCategoryProjection {
    UUID getId();

    String getName();

    Set<Report> getReports();

    String getDescription();
}
