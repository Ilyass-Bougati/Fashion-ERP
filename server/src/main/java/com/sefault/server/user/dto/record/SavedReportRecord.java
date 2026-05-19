package com.sefault.server.user.dto.record;

import java.time.LocalDateTime;
import java.util.UUID;

public record SavedReportRecord(UUID id, String url, LocalDateTime expiresAt) {}
