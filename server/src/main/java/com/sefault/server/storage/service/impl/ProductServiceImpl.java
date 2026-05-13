package com.sefault.server.storage.service.impl;

import com.sefault.server.exception.NotFoundException;
import com.sefault.server.image.repository.ImageRepository;
import com.sefault.server.storage.dto.record.ProductRecord;
import com.sefault.server.storage.entity.Product;
import com.sefault.server.storage.mapper.ProductMapper;
import com.sefault.server.storage.repository.ProductCategoryRepository;
import com.sefault.server.storage.repository.ProductRepository;
import com.sefault.server.storage.service.ProductService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    private final ProductCategoryRepository productCategoryRepository;
    private final ImageRepository imageRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<ProductRecord> getAll(Pageable pageable) {
        return productRepository.findAllBy(pageable).map(productMapper::projectionToRecord);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductRecord getById(UUID id) {
        return productRepository
                .getProductProjectionById(id)
                .map(productMapper::projectionToRecord)
                .orElseThrow(() -> new NotFoundException("Product not found with id : " + id));
    }

    @Override
    public ProductRecord save(ProductRecord record) {
        Product product = productMapper.toEntity(record);
        product.setImage(imageRepository.getReferenceById(record.imageId()));
        product.setProductCategory(productCategoryRepository.getReferenceById(record.productCategoryId()));
        Product saved = productRepository.save(product);
        return productMapper.entityToRecord(saved);
    }

    @Override
    public ProductRecord update(UUID id, ProductRecord record) {
        Product product = findOrThrow(id);
        productMapper.updateEntityFromRecord(record, product);
        product.setImage(imageRepository.getReferenceById(record.imageId()));
        product.setProductCategory(productCategoryRepository.getReferenceById(record.productCategoryId()));
        return productMapper.entityToRecord(productRepository.save(product));
    }

    @Override
    public void delete(UUID id) {
        if (!productRepository.existsById(id)) {
            throw new NotFoundException("Product not found by id : " + id);
        } else {
            productRepository.deleteById(id);
        }
    }

    private Product findOrThrow(UUID id) throws NotFoundException {
        return productRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found with id : " + id));
    }
}
