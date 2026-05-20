package com.sefault.server.user.entity;

import com.sefault.server.user.entity.id.UserReportId;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.*;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class UserReport {
    @EmbeddedId
    private UserReportId id = new UserReportId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    private User user;

    @Column(name = "user_id", insertable = false, updatable = false)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("reportId")
    private Report report;

    @Column(name = "report_id", insertable = false, updatable = false)
    private UUID reportId;

    @CreationTimestamp
    @Immutable
    @Column(updatable = false)
    private LocalDateTime accessedAt;
}
