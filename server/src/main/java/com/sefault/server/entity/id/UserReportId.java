package com.sefault.server.entity.id;

import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Embeddable
@EqualsAndHashCode
public class UserReportId {
    private UUID userId;
    private UUID reportId;
}
