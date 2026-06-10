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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @GetMapping
    @PreAuthorize("hasAuthority(@authorities.listProductsAuthority)")
    public ResponseEntity<Page<ProductRecord>> getAll(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(productService.getAll(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority(@authorities.getProductAuthority)")
    public ProductRecord getById(@PathVariable UUID id) {
        return productService.getById(id);
    }

    @PostMapping
    @PreAuthorize("hasAuthority(@authorities.createProductAuthority)")
    public ProductRecord save(@Valid @RequestBody ProductRecord productRecord) {
        return productService.save(productRecord);
    }

    @PutMapping
    @PreAuthorize("hasAuthority(@authorities.updateProductAuthority)")
    public ProductRecord update(@Valid @RequestBody ProductRecord productRecord) {
        return productService.update(productRecord.id(), productRecord);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority(@authorities.deleteProductAuthority)")
    public void delete(@PathVariable UUID id) {
        productService.delete(id);
    }
}
