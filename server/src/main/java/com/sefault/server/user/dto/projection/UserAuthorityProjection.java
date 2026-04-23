package com.sefault.server.user.dto.projection;

import com.sefault.server.user.entity.Authority;
import com.sefault.server.user.entity.User;
import com.sefault.server.user.entity.id.UserAuthorityId;

import java.time.LocalDateTime;

public interface UserAuthorityProjection {
    UserAuthorityId getId();

    User getUser();

    Authority getAuthority();

    User getGrantedBy();

    LocalDateTime getGrantedAt();
}