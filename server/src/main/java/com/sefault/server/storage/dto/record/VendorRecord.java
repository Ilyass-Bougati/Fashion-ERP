package com.sefault.server.storage.dto.record;

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
        UUID productId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {}
