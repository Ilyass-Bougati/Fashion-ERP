package com.sefault.server.entity;

import com.sefault.server.enums.ReportStatus;
import com.sefault.server.enums.ReportType;
import jakarta.persistence.*;
import jakarta.persistence.CascadeType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;
import lombok.Setter;
import org.hibernate.annotations.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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
    private ReportType type;

    @NotNull
    private ReportStatus status;

    @NotEmpty
    private String objectKey;

    @NotEmpty
    private String bucketName = "reports";

    @NotEmpty
    private String contentType;

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL)
    private List<UserReport> userReports = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "report_report_category",
            joinColumns = @JoinColumn(name = "report_id"),
            inverseJoinColumns = @JoinColumn(name = "report_category_id")
    )
    private Set<ReportCategory> categories;

    @CreationTimestamp
    private LocalDateTime generatedAt;
}
