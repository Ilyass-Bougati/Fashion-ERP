package com.sefault.server.hr.dto.projection;

import java.time.LocalDateTime;
import java.util.UUID;

public interface EmployeeProjection {
    UUID getId();

    UUID getImageId();

    String getFirstName();

    String getLastName();

    String getPhoneNumber();

    String getCIN();

    String getEmail();

    Boolean getActive();

    Double getSalary();

    Double getCommission();

    LocalDateTime getHiredAt();

    LocalDateTime getTerminatedAt();

    LocalDateTime getCreatedAt();

    LocalDateTime getUpdatedAt();
}
