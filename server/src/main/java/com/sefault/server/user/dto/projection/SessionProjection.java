package com.sefault.server.user.dto.projection;

import java.time.LocalDateTime;
import java.util.UUID;

public interface SessionProjection {
    UUID getId();

    String getToken();

    UUID getUserId();

    Boolean getActive();

    LocalDateTime getOpenedAt();

    LocalDateTime getClosedAt();
}
