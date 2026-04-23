package com.sefault.server.hr.dto.projection;

import com.sefault.server.hr.entity.Employee;
import java.util.UUID;

public interface IsleProjection {
    UUID getId();

    Employee getEmployee();

    String getCode();
}
