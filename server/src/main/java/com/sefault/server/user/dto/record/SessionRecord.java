package com.sefault.server.user.dto.record;

import com.sefault.server.user.entity.User;

import java.time.LocalDateTime;
import java.util.UUID;

public record SessionRecord(
        UUID id,
        String token,
        User user,
        Boolean active,
        LocalDateTime openedAt,
        LocalDateTime closedAt
) {
}

