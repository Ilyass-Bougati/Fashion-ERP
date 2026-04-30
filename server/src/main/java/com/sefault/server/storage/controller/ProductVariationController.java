package com.sefault.server.storage.controller;

import com.sefault.server.storage.dto.projection.ProductVariationProjection;
import com.sefault.server.storage.dto.record.ProductVariationRecord;
import com.sefault.server.storage.service.ProductVariationService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/product-variations")
@RequiredArgsConstructor
public class ProductVariationController {
    private final ProductVariationService productVariationService;

    @GetMapping("/{id}")
    public ProductVariationProjection getById(@PathVariable UUID id) {
        return productVariationService.getById(id);
    }

    @PostMapping
    public ProductVariationRecord save(@RequestBody ProductVariationRecord productVariationRecord) {
        return productVariationService.save(productVariationRecord);
    }

    @PutMapping
    public ProductVariationRecord update(@RequestBody ProductVariationRecord productVariationRecord) {
        return productVariationService.update(productVariationRecord);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        productVariationService.delete(id);
    }
}
