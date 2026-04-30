package com.sefault.server.storage.service.impl;

import com.sefault.server.exception.NotFoundException;
import com.sefault.server.storage.dto.projection.VendorProjection;
import com.sefault.server.storage.dto.record.VendorRecord;
import com.sefault.server.storage.entity.Product;
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
    @Transactional(readOnly = true)
    public Page<VendorProjection> findAllPaginated(Pageable pageable) {
        return vendorRepository.findAllBy(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public VendorProjection getById(UUID id) {
        return vendorRepository
                .getVendorProjectionById(id)
                .orElseThrow(() -> new NotFoundException("Vendor not found by id : " + id));
    }

    @Override
    public VendorRecord save(VendorRecord vendor) {
        final Product product;
        if (vendor.productId() != null && productRepository.existsById(vendor.productId())) {
            product = productRepository.getReferenceById(vendor.productId());
        } else {
            product = null;
        }
        return vendorMapper.entityToRecord(vendorRepository.save(Vendor.builder()
                .companyName(vendor.companyName())
                .email(vendor.email())
                .contactName(vendor.contactName())
                .phoneNumber(vendor.phoneNumber())
                .paymentTerms(vendor.paymentTerms())
                .active(vendor.active())
                .product(product)
                .build()));
    }

    @Override
    public VendorRecord update(VendorRecord vendor) {
        if (vendor.id() == null || !productRepository.existsById(vendor.id())) {
            throw new NotFoundException("Vendor not found by id : " + vendor.id());
        } else {
            final Product product;
            if (vendor.productId() != null && productRepository.existsById(vendor.productId())) {
                product = productRepository.getReferenceById(vendor.productId());
            } else {
                product = null;
            }
            return vendorMapper.entityToRecord(vendorRepository.save(Vendor.builder()
                    .id(vendor.id())
                    .companyName(vendor.companyName())
                    .email(vendor.email())
                    .contactName(vendor.contactName())
                    .phoneNumber(vendor.phoneNumber())
                    .paymentTerms(vendor.paymentTerms())
                    .active(vendor.active())
                    .product(product)
                    .build()));
        }
    }

    @Override
    public void delete(UUID id) {
        if (vendorRepository.getVendorProjectionById(id).isEmpty()) {
            throw new NotFoundException("Vendor not found by id : " + id);
        } else {
            vendorRepository.deleteById(id);
        }
    }
}
