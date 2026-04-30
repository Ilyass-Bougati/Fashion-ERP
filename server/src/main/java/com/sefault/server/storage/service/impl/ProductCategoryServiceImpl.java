package com.sefault.server.storage.service.impl;

import com.sefault.server.exception.NotFoundException;
import com.sefault.server.storage.dto.projection.ProductCategoryProjection;
import com.sefault.server.storage.dto.record.ProductCategoryRecord;
import com.sefault.server.storage.entity.ProductCategory;
import com.sefault.server.storage.mapper.ProductCategoryMapper;
import com.sefault.server.storage.repository.ProductCategoryRepository;
import com.sefault.server.storage.service.ProductCategoryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductCategoryServiceImpl implements ProductCategoryService {
    private final ProductCategoryRepository productCategoryRepository;
    private final ProductCategoryMapper productCategoryMapper;

    @Override
    @Transactional(readOnly = true)
    public ProductCategoryProjection getById(UUID id) {
        return productCategoryRepository
                .getProductCategoryProjectionById(id)
                .orElseThrow(() -> new NotFoundException("Product category not found by id : " + id));
    }

    @Override
    public ProductCategoryRecord save(ProductCategoryRecord productCategory) {
        return productCategoryMapper.entityToRecord(productCategoryRepository.save(ProductCategory.builder()
                .name(productCategory.name())
                .description(productCategory.description())
                .build()));
    }

    @Override
    public ProductCategoryRecord update(ProductCategoryRecord productCategory) {
        if (productCategory.id() == null || !productCategoryRepository.existsById(productCategory.id())) {
            throw new NotFoundException("Product category not found by id : " + productCategory.id());
        } else {
            return productCategoryMapper.entityToRecord(productCategoryRepository.save(ProductCategory.builder()
                    .id(productCategory.id())
                    .name(productCategory.name())
                    .description(productCategory.description())
                    .build()));
        }
    }

    @Override
    public void delete(UUID id) {
        if (productCategoryRepository.getProductCategoryProjectionById(id).isEmpty()) {
            throw new NotFoundException("Product category not found by id : " + id);
        } else {
            productCategoryRepository.deleteById(id);
        }
    }
}
