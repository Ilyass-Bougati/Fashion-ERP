package com.sefault.server.user.dto.projection;

import com.sefault.server.user.entity.User;

import java.time.LocalDateTime;
import java.util.UUID;

public interface SessionProjection {
    UUID getId();

    String getToken();

    User getUser();

    Boolean getActive();

    LocalDateTime getOpenedAt();

    LocalDateTime getClosedAt();
}