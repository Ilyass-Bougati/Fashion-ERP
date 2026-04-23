package com.sefault.server.user.dto.record;

import java.time.LocalDateTime;
import java.util.UUID;

public record SessionRecord(
        UUID id, String token, UUID userId, Boolean active, LocalDateTime openedAt, LocalDateTime closedAt) {}
