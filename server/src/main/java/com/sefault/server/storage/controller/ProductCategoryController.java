package com.sefault.server.storage.controller;

import com.sefault.server.storage.dto.record.ProductCategoryRecord;
import com.sefault.server.storage.service.ProductCategoryService;
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
@RequestMapping("api/v1/product-categories")
@RequiredArgsConstructor
public class ProductCategoryController {
    private final ProductCategoryService productCategoryService;

    @GetMapping
    @PreAuthorize("hasAuthority(@authorities.listProductCategoriesAuthority)")
    public ResponseEntity<Page<ProductCategoryRecord>> getAll(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(productCategoryService.getAll(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority(@authorities.getProductCategoryAuthority)")
    public ProductCategoryRecord getById(@PathVariable UUID id) {
        return productCategoryService.getById(id);
    }

    @PostMapping
    @PreAuthorize("hasAuthority(@authorities.createProductCategoryAuthority)")
    public ProductCategoryRecord save(@Valid @RequestBody ProductCategoryRecord productCategoryRecord) {
        return productCategoryService.save(productCategoryRecord);
    }

    @PutMapping
    @PreAuthorize("hasAuthority(@authorities.updateProductCategoryAuthority)")
    public ProductCategoryRecord update(@Valid @RequestBody ProductCategoryRecord productCategoryRecord) {
        return productCategoryService.update(productCategoryRecord.id(), productCategoryRecord);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority(@authorities.deleteProductCategoryAuthority)")
    public void delete(@PathVariable UUID id) {
        productCategoryService.delete(id);
    }
}
