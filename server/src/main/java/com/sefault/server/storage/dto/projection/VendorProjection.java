package com.sefault.server.storage.dto.projection;

import java.time.LocalDateTime;
import java.util.UUID;

public interface VendorProjection {
    UUID getId();

    String getCompanyName();

    String getEmail();

    String getContactName();

    String getPhoneNumber();

    String getPaymentTerms();

    Boolean getActive();

    UUID getProductId();

    LocalDateTime getCreatedAt();

    LocalDateTime getUpdatedAt();
}
