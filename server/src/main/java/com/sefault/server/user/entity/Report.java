package com.sefault.server.user.entity;

import com.sefault.server.user.enums.ReportCategory;
import com.sefault.server.user.enums.ReportStatus;
import com.sefault.server.user.enums.ReportType;
import jakarta.persistence.*;
import jakarta.persistence.CascadeType;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotEmpty
    private String title;

    @NotNull
    @Enumerated(EnumType.STRING)
    private ReportType type;

    @NotNull
    @Enumerated(EnumType.STRING)
    private ReportStatus status;

    @NotEmpty
    private String objectKey;

    @NotEmpty
    private String bucketName = "reports";

    @NotEmpty
    private String contentType;

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL)
    private List<UserReport> userReports = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private ReportCategory category;

    @CreationTimestamp
    @Immutable
    @Column(updatable = false)
    private LocalDateTime generatedAt;
}
