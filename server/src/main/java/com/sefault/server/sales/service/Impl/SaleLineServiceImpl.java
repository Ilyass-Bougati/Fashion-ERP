package com.sefault.server.sales.service.Impl;

import com.sefault.server.exception.NotFoundException;
import com.sefault.server.sales.dto.record.SaleLineRecord;
import com.sefault.server.sales.entity.SaleLine;
import com.sefault.server.sales.entity.id.SaleLineId;
import com.sefault.server.sales.mapper.SaleLineMapper;
import com.sefault.server.sales.repository.SaleLineRepository;
import com.sefault.server.sales.repository.SaleRepository;
import com.sefault.server.sales.service.SaleLineService;
import com.sefault.server.storage.entity.ProductVariation;
import com.sefault.server.storage.repository.ProductVariationRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SaleLineServiceImpl implements SaleLineService {

    private final SaleLineRepository saleLineRepository;
    private final SaleLineMapper saleLineMapper;
    private final SaleRepository saleRepository;
    private final ProductVariationRepository productVariationRepository;

    @Override
    public SaleLineRecord create(SaleLineRecord record) {
        ProductVariation product = productVariationRepository
                .findById(record.productVariationId())
                .orElseThrow(() -> new NotFoundException("Product variation not found"));

        if (product.getQuantity() < record.quantity()) {
            throw new RuntimeException("Insufficient stock for product variation");
        }

        product.setQuantity(product.getQuantity() - record.quantity());
        productVariationRepository.save(product);

        SaleLine saleLine = saleLineMapper.toEntity(record);
        saleLine.setSale(saleRepository.getReferenceById(record.saleId()));
        saleLine.setProductVariation(product);
        return saleLineMapper.entityToRecord(saleLineRepository.save(saleLine));
    }

    @Override
    @Transactional(readOnly = true)
    public SaleLineRecord getById(SaleLineId id) {
        return saleLineRepository
                .getSaleLineProjectionById(id)
                .map(saleLineMapper::projectionToRecord)
                .orElseThrow(() -> new NotFoundException("SaleLine not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SaleLineRecord> getBySaleId(UUID saleId) {
        return saleLineRepository.findAllBySaleId(saleId).stream()
                .map(saleLineMapper::projectionToRecord)
                .toList();
    }

    @Override
    public SaleLineRecord update(SaleLineId id, SaleLineRecord record) {
        SaleLine saleLine = findEntityOrThrow(id);

        ProductVariation product = productVariationRepository
                .findById(record.productVariationId())
                .orElseThrow(() -> new NotFoundException("Product variation not found"));

        int quantityDifference = record.quantity() - saleLine.getQuantity();
        if (quantityDifference > 0 && product.getQuantity() < quantityDifference) {
            throw new RuntimeException("Insufficient stock for update");
        }

        product.setQuantity(product.getQuantity() - quantityDifference);
        productVariationRepository.save(product);

        saleLineMapper.updateEntityFromRecord(record, saleLine);
        return saleLineMapper.entityToRecord(saleLineRepository.save(saleLine));
    }

    @Override
    public void delete(SaleLineId id) {
        SaleLine saleLine = findEntityOrThrow(id);

        ProductVariation product = saleLine.getProductVariation();
        product.setQuantity(product.getQuantity() + saleLine.getQuantity());
        productVariationRepository.save(product);

        saleLineRepository.deleteByCompositeId(id.getSaleId(), id.getProductVariationId());
    }

    private SaleLine findEntityOrThrow(SaleLineId id) {
        return saleLineRepository.findById(id).orElseThrow(() -> new NotFoundException("SaleLine not found"));
    }
}
