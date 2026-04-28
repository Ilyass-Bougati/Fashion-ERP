package com.sefault.server.user.dto.record;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserRecord(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {}
