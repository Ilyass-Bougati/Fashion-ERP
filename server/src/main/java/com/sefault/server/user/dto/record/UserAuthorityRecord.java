package com.sefault.server.user.dto.record;

import com.sefault.server.user.entity.Authority;
import com.sefault.server.user.entity.User;
import com.sefault.server.user.entity.id.UserAuthorityId;

import java.time.LocalDateTime;

public record UserAuthorityRecord(
        UserAuthorityId id,
        User user,
        Authority authority,
        User grantedBy,
        LocalDateTime grantedAt
) {
}

