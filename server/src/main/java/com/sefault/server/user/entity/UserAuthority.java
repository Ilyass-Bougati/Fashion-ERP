package com.sefault.server.user.entity;

import com.sefault.server.user.entity.id.UserAuthorityId;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class UserAuthority {
    @EmbeddedId
    private UserAuthorityId id = new UserAuthorityId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("authorityId")
    private Authority authority;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "granted_by_id")
    private User grantedBy;

    @CreationTimestamp
    private LocalDateTime grantedAt;
}
