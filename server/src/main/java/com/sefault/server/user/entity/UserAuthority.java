package com.sefault.server.user.entity;

import com.sefault.server.user.entity.id.UserAuthorityId;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.*;
import org.hibernate.annotations.*;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAuthority {
    @EmbeddedId
    @Builder.Default
    private UserAuthorityId id = new UserAuthorityId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    private User user;

    @Column(name = "user_id", insertable = false, updatable = false)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("authorityId")
    private Authority authority;

    @Column(name = "authority_id", insertable = false, updatable = false)
    private UUID authorityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "granted_by_id")
    private User grantedBy;

    @Column(name = "granted_by_id", insertable = false, updatable = false)
    private UUID grantedById;

    @CreationTimestamp
    @Immutable
    @Column(updatable = false)
    private LocalDateTime grantedAt;
}
