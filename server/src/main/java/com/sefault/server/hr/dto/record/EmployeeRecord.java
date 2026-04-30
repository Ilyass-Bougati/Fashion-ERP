package com.sefault.server.hr.dto.record;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.NonNull;

public record EmployeeRecord(
        UUID id,
        @NonNull UUID imageId,
        String firstName,
        String lastName,
        String phoneNumber,
        String CIN,
        String email,
        Boolean active,
        Double salary,
        Double commission,
        LocalDateTime hiredAt,
        LocalDateTime terminatedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {}
