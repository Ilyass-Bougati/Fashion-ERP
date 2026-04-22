package com.sefault.server.user.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class ReportCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotEmpty
    private String name;

    @ManyToMany(mappedBy = "categories")
    private Set<Report> reports;

    @NotEmpty
    @Column(columnDefinition = "TEXT")
    private String description;
}
