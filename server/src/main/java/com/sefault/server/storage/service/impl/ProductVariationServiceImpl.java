package com.sefault.server.storage.service.impl;

import com.sefault.server.exception.NotFoundException;
import com.sefault.server.image.entity.Image;
import com.sefault.server.image.repository.ImageRepository;
import com.sefault.server.storage.dto.projection.ProductVariationProjection;
import com.sefault.server.storage.dto.record.ProductVariationRecord;
import com.sefault.server.storage.entity.Product;
import com.sefault.server.storage.entity.ProductVariation;
import com.sefault.server.storage.mapper.ProductVariationMapper;
import com.sefault.server.storage.repository.ProductRepository;
import com.sefault.server.storage.repository.ProductVariationRepository;
import com.sefault.server.storage.service.ProductVariationService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductVariationServiceImpl implements ProductVariationService {
    private final ProductVariationRepository productVariationRepository;
    private final ProductVariationMapper productVariationMapper;

    private final ProductRepository productRepository;
    private final ImageRepository imageRepository;

    @Override
    @Transactional(readOnly = true)
    public ProductVariationProjection getById(UUID id) {
        return productVariationRepository
                .getProductVariationProjectionById(id)
                .orElseThrow(() -> new NotFoundException("Product variation not found by id : " + id));
    }

    @Override
    public ProductVariationRecord save(ProductVariationRecord productVariation) {
        final Product product;
        final Image image;

        if (productVariation.productId() != null
                && productVariationRepository.existsById(productVariation.productId())) {
            product = productRepository.getReferenceById(productVariation.productId());
        } else {
            product = null;
        }

        if (productVariation.imageId() != null && imageRepository.existsById(productVariation.imageId())) {
            image = imageRepository.getReferenceById(productVariation.imageId());
        } else {
            image = null;
        }

        return productVariationMapper.entityToRecord(ProductVariation.builder()
                .product(product)
                .image(image)
                .sku(productVariation.sku())
                .price(productVariation.price())
                .quantity(productVariation.quantity())
                .build());
    }

    @Override
    public ProductVariationRecord update(ProductVariationRecord productVariation) {
        if (productVariation.id() == null || !productVariationRepository.existsById(productVariation.id())) {
            throw new NotFoundException("Product variation not found by id : " + productVariation.id());
        } else {
            final Product product;
            final Image image;

            if (productVariation.productId() != null
                    && productVariationRepository.existsById(productVariation.productId())) {
                product = productRepository.getReferenceById(productVariation.productId());
            } else {
                product = null;
            }

            if (productVariation.imageId() != null && imageRepository.existsById(productVariation.imageId())) {
                image = imageRepository.getReferenceById(productVariation.imageId());
            } else {
                image = null;
            }

            return productVariationMapper.entityToRecord(ProductVariation.builder()
                    .id(productVariation.id())
                    .product(product)
                    .image(image)
                    .sku(productVariation.sku())
                    .price(productVariation.price())
                    .quantity(productVariation.quantity())
                    .build());
        }
    }

    @Override
    public void delete(UUID id) {
        if (productVariationRepository.getProductVariationProjectionById(id).isEmpty()) {
            throw new NotFoundException("Product variation not found by id : " + id);
        } else {
            productVariationRepository.deleteById(id);
        }
    }
}
