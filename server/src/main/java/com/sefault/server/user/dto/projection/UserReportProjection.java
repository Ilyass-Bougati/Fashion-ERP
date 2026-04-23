package com.sefault.server.user.dto.projection;

import com.sefault.server.user.entity.Report;
import com.sefault.server.user.entity.User;
import com.sefault.server.user.entity.id.UserReportId;
import java.time.LocalDateTime;

public interface UserReportProjection {
    UserReportId getId();

    User getUser();

    Report getReport();

    LocalDateTime getAccessedAt();
}
