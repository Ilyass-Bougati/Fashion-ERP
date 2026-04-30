package com.sefault.server.storage.service.impl;

import com.sefault.server.exception.NotFoundException;
import com.sefault.server.image.entity.Image;
import com.sefault.server.image.repository.ImageRepository;
import com.sefault.server.storage.dto.projection.ProductProjection;
import com.sefault.server.storage.dto.record.ProductRecord;
import com.sefault.server.storage.entity.Product;
import com.sefault.server.storage.mapper.ProductMapper;
import com.sefault.server.storage.repository.ProductCategoryRepository;
import com.sefault.server.storage.repository.ProductRepository;
import com.sefault.server.storage.service.ProductService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
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
    public ProductProjection getById(UUID id) {
        return productRepository
                .getProductProjectionById(id)
                .orElseThrow(() -> new NotFoundException("Product not found by id : " + id));
    }

    @Override
    public ProductRecord save(ProductRecord product) {
        final Image image;
        if (product.imageId() != null && imageRepository.existsById(product.imageId())) {
            image = imageRepository.getReferenceById(product.imageId());
        } else {
            image = null;
        }
        return productMapper.entityToRecord(productRepository.save(Product.builder()
                .name(product.name())
                .productCategory(productCategoryRepository.getReferenceById(product.productCategoryId()))
                .image(image)
                .build()));
    }

    @Override
    public ProductRecord update(ProductRecord product) {
        if (product.id() == null || !productRepository.existsById(product.id())) {
            throw new NotFoundException("Product not found by id : " + product.id());
        } else {
            final Image image;
            if (product.imageId() != null && imageRepository.existsById(product.imageId())) {
                image = imageRepository.getReferenceById(product.imageId());
            } else {
                image = null;
            }
            return productMapper.entityToRecord(productRepository.save(Product.builder()
                    .id(product.id())
                    .name(product.name())
                    .productCategory(productCategoryRepository.getReferenceById(product.productCategoryId()))
                    .image(image)
                    .build()));
        }
    }

    @Override
    public void delete(UUID id) {
        if (productRepository.getProductProjectionById(id).isEmpty()) {
            throw new NotFoundException("Product not found by id : " + id);
        } else {
            productRepository.deleteById(id);
        }
    }
}
