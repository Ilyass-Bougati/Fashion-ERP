package com.sefault.server.storage.service.impl;

import com.sefault.server.exception.NotFoundException;
import com.sefault.server.storage.dto.record.VendorRecord;
import com.sefault.server.storage.entity.Vendor;
import com.sefault.server.storage.mapper.VendorMapper;
import com.sefault.server.storage.repository.ProductRepository;
import com.sefault.server.storage.repository.VendorRepository;
import com.sefault.server.storage.service.VendorService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class VendorServiceImpl implements VendorService {
    private final VendorRepository vendorRepository;
    private final VendorMapper vendorMapper;

    private final ProductRepository productRepository;

    @Override
    public Page<VendorRecord> findAllPaginated(Pageable pageable) {
        return vendorRepository.findAllBy(pageable).map(vendorMapper::projectionToRecord);
    }

    @Override
    @Transactional(readOnly = true)
    public VendorRecord getById(UUID id) {
        return vendorRepository
                .getVendorProjectionById(id)
                .map(vendorMapper::projectionToRecord)
                .orElseThrow(() -> new NotFoundException("Vendor not found with id : " + id));
    }

    @Override
    public VendorRecord save(VendorRecord vendor) {
        Vendor v = vendorMapper.toEntity(vendor);
        v.setProduct(productRepository.getReferenceById(vendor.productId()));
        Vendor saved = vendorRepository.save(v);
        return vendorMapper.entityToRecord(saved);
    }

    @Override
    public VendorRecord update(UUID id, VendorRecord vendor) {
        Vendor v = findOrThrow(id);
        vendorMapper.updateEntityFromRecord(vendor, v);
        v.setProduct(productRepository.getReferenceById(vendor.productId()));
        return vendorMapper.entityToRecord(vendorRepository.save(v));
    }

    @Override
    public void delete(UUID id) {
        if (!vendorRepository.existsById(id)) {
            throw new NotFoundException("Vendor not found by id : " + id);
        } else {
            vendorRepository.deleteById(id);
        }
    }

    private Vendor findOrThrow(UUID id) throws NotFoundException {
        return vendorRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException("Vendor not found with id : " + id));
    }
}
