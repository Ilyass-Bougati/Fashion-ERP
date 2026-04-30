package com.sefault.server.storage.controller;

import com.sefault.server.storage.dto.projection.ProductCategoryProjection;
import com.sefault.server.storage.dto.record.ProductCategoryRecord;
import com.sefault.server.storage.service.ProductCategoryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/product-categories")
@RequiredArgsConstructor
public class ProductCategoryController {
    private final ProductCategoryService productCategoryService;

    @GetMapping("/{id}")
    public ProductCategoryProjection getById(@PathVariable UUID id) {
        return productCategoryService.getById(id);
    }

    @PostMapping
    public ProductCategoryRecord save(@RequestBody ProductCategoryRecord productCategoryRecord) {
        return productCategoryService.save(productCategoryRecord);
    }

    @PutMapping
    public ProductCategoryRecord update(@RequestBody ProductCategoryRecord productCategoryRecord) {
        return productCategoryService.update(productCategoryRecord);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        productCategoryService.delete(id);
    }
}
