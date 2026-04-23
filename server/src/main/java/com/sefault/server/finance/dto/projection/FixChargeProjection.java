package com.sefault.server.finance.dto.projection;

import java.time.LocalDateTime;
import java.util.UUID;

public interface FixChargeProjection {
    UUID getId();

    String getName();

    String getDescription();

    Double getAmount();

    Boolean getActive();

    LocalDateTime getCreatedAt();
}