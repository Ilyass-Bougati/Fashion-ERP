package com.sefault.server.storage.controller;

import com.sefault.server.storage.dto.projection.ProductProjection;
import com.sefault.server.storage.dto.record.ProductRecord;
import com.sefault.server.storage.service.ProductService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @GetMapping("/{id}")
    public ProductProjection getById(@PathVariable UUID id) {
        return productService.getById(id);
    }

    @PostMapping
    public ProductRecord save(@RequestBody ProductRecord productRecord) {
        return productService.save(productRecord);
    }

    @PutMapping
    public ProductRecord update(@RequestBody ProductRecord productRecord) {
        return productService.update(productRecord);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        productService.delete(id);
    }
}
