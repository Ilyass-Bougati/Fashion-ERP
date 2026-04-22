package com.sefault.server.hr.entity;

import com.sefault.server.finance.entity.Payroll;
import com.sefault.server.image.entity.Image;
import com.sefault.server.sales.entity.Sale;
import jakarta.persistence.*;
import jakarta.persistence.CascadeType;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id")
    private Image image;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Payroll> payrolls = new ArrayList<>();

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Sale> sales = new ArrayList<>();
    
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Isle> isles = new ArrayList<>();

    @NotEmpty
    private String firstName;

    @NotEmpty
    private String lastName;

    @NotEmpty
    private String phoneNumber;

    @NotEmpty
    private String CIN;

    @Email
    @NotEmpty
    private String email;

    @NotNull
    private Boolean active;

    @NotNull
    @Positive
    private Double salary;

    @NotNull
    @Min(0)
    @Max(1)
    private Double commission;

    @NotNull
    private LocalDateTime hiredAt;

    private LocalDateTime terminatedAt = null;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
