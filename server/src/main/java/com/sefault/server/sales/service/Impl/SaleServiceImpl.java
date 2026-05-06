package com.sefault.server.sales.service.Impl;

import com.sefault.server.exception.NotFoundException;
import com.sefault.server.finance.dto.record.TransactionRecord;
import com.sefault.server.finance.enums.TransactionType;
import com.sefault.server.finance.service.TransactionService;
import com.sefault.server.hr.repository.EmployeeRepository;
import com.sefault.server.sales.dto.record.SaleRecord;
import com.sefault.server.sales.entity.Sale;
import com.sefault.server.sales.mapper.SaleMapper;
import com.sefault.server.sales.repository.SaleRepository;
import com.sefault.server.sales.service.SaleService;
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

    @Override
    public SaleRecord update(UUID id, SaleRecord record) {
        Sale sale = findEntityOrThrow(id);
        saleMapper.updateEntityFromRecord(record, sale);
        if (record.employeeId() != null) {
            sale.setEmployee(employeeRepository.getReferenceById(record.employeeId()));
        }
        return saleMapper.entityToRecord(saleRepository.save(sale));
    }

    @Override
    public void delete(UUID id) {
        if (!saleRepository.existsById(id)) {
            throw new NotFoundException("Sale not found with id: " + id);
        }
        saleRepository.deleteById(id);
    }

    private Sale findEntityOrThrow(UUID id) {
        return saleRepository.findById(id).orElseThrow(() -> new NotFoundException("Sale not found with id: " + id));
    }

    @Override
    public TransactionRecord checkout(UUID id) {
        Sale sale = findEntityOrThrow(id);

        if (sale.getTransactions() != null && !sale.getTransactions().isEmpty()) {
            throw new RuntimeException("This sale has already been checked out.");
        }

        double totalAmount = sale.getSaleLines().stream()
                .mapToDouble(line -> line.getQuantity() * line.getSaleAtPrice())
                .sum();

        double finalAmount = totalAmount * (1.0 - sale.getDiscount());

        TransactionRecord transactionRecord =
                new TransactionRecord(null, TransactionType.RECEIVED, sale.getId(), finalAmount, null);

        return transactionService.createTransaction(transactionRecord);
    }
}
