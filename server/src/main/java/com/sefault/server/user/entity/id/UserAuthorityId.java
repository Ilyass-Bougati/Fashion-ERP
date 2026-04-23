package com.sefault.server.user.entity.id;

import jakarta.persistence.Embeddable;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@Embeddable
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class UserAuthorityId {
    private UUID userId;
    private UUID authorityId;
}
