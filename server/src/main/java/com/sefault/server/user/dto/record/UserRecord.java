package com.sefault.server.user.dto.record;

import com.sefault.server.user.entity.Session;
import com.sefault.server.user.entity.UserAuthority;
import com.sefault.server.user.entity.UserReport;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record UserRecord(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String password,
        List<Session> sessions,
        List<UserAuthority> userAuthorities,
        List<UserReport> userReports,
        List<UserAuthority> grantedAuthorities,
        String phoneNumber,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

