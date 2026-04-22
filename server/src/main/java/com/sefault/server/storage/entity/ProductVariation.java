package com.sefault.server.storage.entity;

import com.sefault.server.image.entity.Image;
import com.sefault.server.sales.entity.SaleLine;
import jakarta.persistence.*;
import jakarta.persistence.CascadeType;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotEmpty
    private String sku;

    @NotNull
    private Double price;

    @OneToMany(mappedBy = "productVariation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SaleLine> saleLines = new ArrayList<>();

    @NotNull
    private Integer quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id")
    private Image image;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
