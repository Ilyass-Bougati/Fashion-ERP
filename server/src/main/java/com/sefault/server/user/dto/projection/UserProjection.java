package com.sefault.server.user.dto.projection;

import java.time.LocalDateTime;
import java.util.UUID;

public interface UserProjection {
    UUID getId();

    String getFirstName();

    String getLastName();

    String getEmail();


    String getPhoneNumber();

    Boolean getActive();

    LocalDateTime getCreatedAt();

    LocalDateTime getUpdatedAt();
}
