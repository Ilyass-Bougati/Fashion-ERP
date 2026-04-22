package com.sefault.server.user.entity.id;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Embeddable
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class UserReportId {
    private UUID userId;
    private UUID reportId;
}
