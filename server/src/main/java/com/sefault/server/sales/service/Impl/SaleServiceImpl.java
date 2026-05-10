package com.sefault.server.sales.service.Impl;

import com.sefault.server.exception.NotFoundException;
import com.sefault.server.finance.dto.record.TransactionRecord;
import com.sefault.server.finance.enums.TransactionType;
import com.sefault.server.finance.service.TransactionService;
import com.sefault.server.hr.repository.EmployeeRepository;
import com.sefault.server.sales.SaleStatus;
import com.sefault.server.sales.dto.record.SaleRecord;
import com.sefault.server.sales.entity.Sale;
import com.sefault.server.sales.entity.SaleLine;
import com.sefault.server.sales.mapper.SaleMapper;
import com.sefault.server.sales.repository.SaleRepository;
import com.sefault.server.sales.service.SaleService;
import com.sefault.server.storage.repository.ProductVariationRepository;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SaleServiceImpl implements SaleService {

    private final SaleRepository saleRepository;
    private final SaleMapper saleMapper;
    private final EmployeeRepository employeeRepository;
    private final TransactionService transactionService;
    private final ProductVariationRepository productVariationRepository;

    @Override
    public SaleRecord create(SaleRecord record) {
        Sale sale = saleMapper.toEntity(record);
        if (record.employeeId() != null) {
            sale.setEmployee(employeeRepository.getReferenceById(record.employeeId()));
        }
        return saleMapper.entityToRecord(saleRepository.save(sale));
    }

    @Override
    @Transactional(readOnly = true)
    public SaleRecord getById(UUID id) {
        return saleRepository
                .getSaleProjectionById(id)
                .map(saleMapper::projectionToRecord)
                .orElseThrow(() -> new NotFoundException("Sale not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SaleRecord> getAll(Pageable pageable) {
        return saleRepository.findAllBy(pageable).map(saleMapper::projectionToRecord);
    }

    private Sale findEntityOrThrow(UUID id) {
        return saleRepository.findById(id).orElseThrow(() -> new NotFoundException("Sale not found with id: " + id));
    }

    @Override
    public TransactionRecord checkout(UUID id) {
        Sale sale = findEntityOrThrow(id);

        if (sale.getStatus() != SaleStatus.PENDING) {
            throw new IllegalStateException("This sale is already completed or refunded.");
        }

        double totalAmount = sale.getSaleLines().stream()
                .mapToDouble(line -> line.getQuantity() * line.getSaleAtPrice())
                .sum();

        double finalAmount = totalAmount * (1.0 - sale.getDiscount());

        TransactionRecord transactionRecord =
                new TransactionRecord(null, TransactionType.RECEIVED, sale.getId(), finalAmount, null);

        sale.setStatus(SaleStatus.COMPLETED);
        saleRepository.save(sale);

        return transactionService.createTransaction(transactionRecord);
    }

    @Override
    public TransactionRecord refund(UUID id) {
        Sale sale = findEntityOrThrow(id);

        if (sale.getStatus() == SaleStatus.REFUNDED) {
            throw new IllegalStateException("This sale has already been refunded.");
        }
        if (sale.getStatus() == SaleStatus.PENDING) {
            throw new IllegalStateException("Cannot refund a sale that is still pending (not checked out).");
        }

        double totalAmount = sale.getSaleLines().stream()
                .mapToDouble(line -> line.getQuantity() * line.getSaleAtPrice())
                .sum();

        double amountToRefund = totalAmount * (1.0 - sale.getDiscount());

        for (SaleLine line : sale.getSaleLines()) {
            productVariationRepository.incrementStock(line.getProductVariation().getId(), line.getQuantity());
        }

        sale.setStatus(SaleStatus.REFUNDED);
        saleRepository.save(sale);

        TransactionRecord refundTransaction =
                new TransactionRecord(null, TransactionType.PAID, sale.getId(), amountToRefund, LocalDateTime.now());

        return transactionService.createTransaction(refundTransaction);
    }
}
