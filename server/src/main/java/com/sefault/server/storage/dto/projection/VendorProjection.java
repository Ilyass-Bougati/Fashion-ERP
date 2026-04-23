package com.sefault.server.storage.dto.projection;

import com.sefault.server.storage.entity.Product;

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

    Product getProduct();

    LocalDateTime getCreatedAt();

    LocalDateTime getUpdatedAt();
}