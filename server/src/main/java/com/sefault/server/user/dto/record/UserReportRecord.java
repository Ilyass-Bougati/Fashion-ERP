package com.sefault.server.user.dto.record;

import com.sefault.server.user.entity.id.UserReportId;
import java.time.LocalDateTime;
import java.util.UUID;

public record UserReportRecord(UserReportId id, UUID userId, UUID reportId, LocalDateTime accessedAt) {}
