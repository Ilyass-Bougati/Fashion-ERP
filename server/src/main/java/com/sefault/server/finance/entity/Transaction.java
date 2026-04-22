package com.sefault.server.finance.entity;

import com.sefault.server.finance.enums.TransactionType;
import com.sefault.server.sales.entity.Sale;
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
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @Enumerated(EnumType.STRING)
    private TransactionType type;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id")
    private Sale sale;

    @OneToOne(mappedBy = "transaction")
    private Payroll payroll;

    @NotNull
    private Double amount;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
