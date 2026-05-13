package com.sefault.server.storage.controller;

import com.sefault.server.storage.dto.record.ProductRecord;
import com.sefault.server.storage.service.ProductService;
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
@RequestMapping("api/v1/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @GetMapping
    public ResponseEntity<Page<ProductRecord>> getAll(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(productService.getAll(pageable));
    }

    @GetMapping("/{id}")
    public ProductRecord getById(@PathVariable UUID id) {
        return productService.getById(id);
    }

    @PostMapping
    public ProductRecord save(@Valid @RequestBody ProductRecord productRecord) {
        return productService.save(productRecord);
    }

    @PutMapping
    public ProductRecord update(@Valid @RequestBody UUID id, @Valid @RequestBody ProductRecord productRecord) {
        return productService.update(id, productRecord);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        productService.delete(id);
    }
}
