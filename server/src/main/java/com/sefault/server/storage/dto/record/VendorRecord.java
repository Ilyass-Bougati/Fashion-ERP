package com.sefault.server.storage.dto.record;

import com.sefault.server.storage.entity.Product;
import java.time.LocalDateTime;
import java.util.UUID;

public record VendorRecord(
        UUID id,
        String companyName,
        String email,
        String contactName,
        String phoneNumber,
        String paymentTerms,
        Boolean active,
        Product product,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {}
