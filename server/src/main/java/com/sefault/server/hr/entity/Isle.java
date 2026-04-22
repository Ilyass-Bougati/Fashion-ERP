package com.sefault.server.hr.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Isle {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @NotEmpty
    private String code;
}
