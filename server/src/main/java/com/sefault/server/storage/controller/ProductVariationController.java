package com.sefault.server.storage.controller;

import com.sefault.server.storage.dto.record.ProductVariationRecord;
import com.sefault.server.storage.service.ProductVariationService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/product-variations")
@RequiredArgsConstructor
public class ProductVariationController {
    private final ProductVariationService productVariationService;

    @GetMapping
    @PreAuthorize("hasAuthority(@authorities.listProductVariationsAuthority)")
    public ResponseEntity<Page<ProductVariationRecord>> getAll(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(productVariationService.getAll(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority(@authorities.getProductVariationAuthority)")
    public ProductVariationRecord getById(@PathVariable UUID id) {
        return productVariationService.getById(id);
    }

    @PostMapping
    @PreAuthorize("hasAuthority(@authorities.createProductVariationAuthority)")
    public ProductVariationRecord save(@Valid @RequestBody ProductVariationRecord productVariationRecord) {
        return productVariationService.save(productVariationRecord);
    }

    @PutMapping
    @PreAuthorize("hasAuthority(@authorities.updateProductVariationAuthority)")
    public ProductVariationRecord update(@Valid @RequestBody ProductVariationRecord productVariationRecord) {
        return productVariationService.update(productVariationRecord.id(), productVariationRecord);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority(@authorities.deleteProductVariationAuthority)")
    public void delete(@PathVariable UUID id) {
        productVariationService.delete(id);
    }
}
