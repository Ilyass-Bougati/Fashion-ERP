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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/product-categories")
@RequiredArgsConstructor
public class ProductCategoryController {
    private final ProductCategoryService productCategoryService;

    @GetMapping
    public ResponseEntity<Page<ProductCategoryRecord>> getAll(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(productCategoryService.getAll(pageable));
    }

    @GetMapping("/{id}")
    public ProductCategoryRecord getById(@PathVariable UUID id) {
        return productCategoryService.getById(id);
    }

    @PostMapping
    public ProductCategoryRecord save(@Valid @RequestBody ProductCategoryRecord productCategoryRecord) {
        return productCategoryService.save(productCategoryRecord);
    }

    @PutMapping
    public ProductCategoryRecord update(
            @Valid @RequestBody UUID id, @Valid @RequestBody ProductCategoryRecord productCategoryRecord) {
        return productCategoryService.update(id, productCategoryRecord);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        productCategoryService.delete(id);
    }
}
