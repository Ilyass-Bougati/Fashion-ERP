package com.sefault.server.hr.dto.projection;

import java.util.UUID;

public interface IsleProjection {
    UUID getId();

    UUID getEmployeeId();

    String getCode();
}
