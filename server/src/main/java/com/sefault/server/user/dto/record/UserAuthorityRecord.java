package com.sefault.server.user.dto.record;

import com.sefault.server.user.entity.id.UserAuthorityId;
import java.time.LocalDateTime;
import java.util.UUID;

public record UserAuthorityRecord(
        UserAuthorityId id, UUID userId, UUID authorityId, UUID grantedById, LocalDateTime grantedAt) {}
