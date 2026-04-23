package com.sefault.server.user.dto.projection;

import java.util.UUID;

public interface ReportCategoryProjection {
    UUID getId();

    String getName();

    String getDescription();
}
