package com.sefault.server.user.dto.record;

import com.sefault.server.user.entity.Report;
import com.sefault.server.user.entity.User;
import com.sefault.server.user.entity.id.UserReportId;
import java.time.LocalDateTime;

public record UserReportRecord(UserReportId id, User user, Report report, LocalDateTime accessedAt) {}
