package com.sefault.server.user.dto.projection;

import com.sefault.server.user.entity.id.UserReportId;
import java.time.LocalDateTime;
import java.util.UUID;

public interface UserReportProjection {
    UserReportId getId();

    UUID getUserId();

    UUID getReportId();

    LocalDateTime getAccessedAt();
}
