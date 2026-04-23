package com.sefault.server.storage.dto.projection;

import java.util.UUID;

public interface ProductCategoryProjection {
    UUID getId();

    String getName();

    String getDescription();
}
