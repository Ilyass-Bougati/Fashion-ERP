package com.sefault.server.user.dto.projection;

import com.sefault.server.user.entity.id.UserAuthorityId;
import java.time.LocalDateTime;
import java.util.UUID;

public interface UserAuthorityProjection {
    UserAuthorityId getId();

    UUID getUserId();

    UUID getAuthorityId();

    UUID getGrantedById();

    LocalDateTime getGrantedAt();
}
