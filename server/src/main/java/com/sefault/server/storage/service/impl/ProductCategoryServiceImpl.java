package com.sefault.server.storage.service.impl;

import com.sefault.server.exception.NotFoundException;
import com.sefault.server.storage.dto.record.ProductCategoryRecord;
import com.sefault.server.storage.entity.ProductCategory;
import com.sefault.server.storage.mapper.ProductCategoryMapper;
import com.sefault.server.storage.repository.ProductCategoryRepository;
import com.sefault.server.storage.service.ProductCategoryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public Page<ProductCategoryRecord> getAll(Pageable pageable) {
        return productCategoryRepository.findAllBy(pageable).map(productCategoryMapper::projectionToRecord);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductCategoryRecord getById(UUID id) {
        return productCategoryRepository
                .getProductCategoryProjectionById(id)
                .map(productCategoryMapper::projectionToRecord)
                .orElseThrow(() -> new NotFoundException("Product category not found with id : " + id));
    }

    @Override
    public ProductCategoryRecord save(ProductCategoryRecord record) {
        ProductCategory productCategory = productCategoryMapper.toEntity(record);
        ProductCategory saved = productCategoryRepository.save(productCategory);
        return productCategoryMapper.entityToRecord(saved);
    }

    @Override
    public ProductCategoryRecord update(UUID id, ProductCategoryRecord record) {
        ProductCategory productCategory = findOrThrow(id);
        productCategoryMapper.updateEntityFromRecord(record, productCategory);
        return productCategoryMapper.entityToRecord(productCategoryRepository.save(productCategory));
    }

    @Override
    public void delete(UUID id) {
        if (!productCategoryRepository.existsById(id)) {
            throw new NotFoundException("Product category not found by id : " + id);
        } else {
            productCategoryRepository.deleteById(id);
        }
    }

    private ProductCategory findOrThrow(UUID id) throws NotFoundException {
        return productCategoryRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException("Product category not found with id : " + id));
    }
}
