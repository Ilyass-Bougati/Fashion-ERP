package com.sefault.server.user.entity;

import com.sefault.server.user.entity.id.UserReportId;
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
public class UserReport {
    @EmbeddedId
    private UserReportId id = new UserReportId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("reportId")
    private Report report;

    @CreationTimestamp
    private LocalDateTime accessedAt;
}
