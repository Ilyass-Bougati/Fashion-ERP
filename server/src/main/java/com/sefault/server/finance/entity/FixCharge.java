package com.sefault.server.finance.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
public class FixCharge {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotEmpty
    private String name;

    @NotEmpty
    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull
    @Min(0)
    private Double amount;

    @NotNull
    private Boolean active;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
