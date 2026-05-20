package com.sefault.server.storage.service.impl;

import com.sefault.server.exception.NotFoundException;
import com.sefault.server.image.repository.ImageRepository;
import com.sefault.server.storage.dto.record.ProductVariationRecord;
import com.sefault.server.storage.entity.ProductVariation;
import com.sefault.server.storage.mapper.ProductVariationMapper;
import com.sefault.server.storage.repository.ProductRepository;
import com.sefault.server.storage.repository.ProductVariationRepository;
import com.sefault.server.storage.service.ProductVariationService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public Page<ProductVariationRecord> getAll(Pageable pageable) {
        return productVariationRepository.findAllBy(pageable).map(productVariationMapper::projectionToRecord);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductVariationRecord getById(UUID id) {
        return productVariationRepository
                .getProductVariationProjectionById(id)
                .map(productVariationMapper::projectionToRecord)
                .orElseThrow(() -> new NotFoundException("Product variation not found with id : " + id));
    }

    @Override
    public ProductVariationRecord save(ProductVariationRecord record) {
        ProductVariation productVariation = productVariationMapper.toEntity(record);
        productVariation.setImage(imageRepository.getReferenceById(record.imageId()));
        productVariation.setProduct(productRepository.getReferenceById(record.productId()));
        ProductVariation saved = productVariationRepository.save(productVariation);
        return productVariationMapper.entityToRecord(saved);
    }

    @Override
    public ProductVariationRecord update(UUID id, ProductVariationRecord record) {
        ProductVariation productVariation = findOrThrow(id);
        productVariationMapper.updateEntityFromRecord(record, productVariation);
        productVariation.setImage(imageRepository.getReferenceById(record.imageId()));
        productVariation.setProduct(productRepository.getReferenceById(record.productId()));
        return productVariationMapper.entityToRecord(productVariationRepository.save(productVariation));
    }

    @Override
    public void delete(UUID id) {
        if (!productVariationRepository.existsById(id)) {
            throw new NotFoundException("Product variation not found by id : " + id);
        } else {
            productVariationRepository.deleteById(id);
        }
    }

    private ProductVariation findOrThrow(UUID id) throws NotFoundException {
        return productVariationRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException("Product variation not found with id : " + id));
    }
}
