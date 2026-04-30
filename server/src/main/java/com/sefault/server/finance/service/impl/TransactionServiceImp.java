package com.sefault.server.finance.service.impl;

import com.sefault.server.exception.NotFoundException;
import com.sefault.server.finance.dto.record.TransactionRecord;
import com.sefault.server.finance.entity.Transaction;
import com.sefault.server.finance.enums.TransactionType;
import com.sefault.server.finance.mapper.TransactionMapper;
import com.sefault.server.finance.repository.TransactionRepository;
import com.sefault.server.finance.service.TransactionService;
import com.sefault.server.sales.repository.SaleRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class TransactionServiceImp implements TransactionService {
    private final TransactionRepository transactionRepository;
    private final SaleRepository saleRepository;

    private final TransactionMapper transactionMapper;

    @Override
    public TransactionRecord createTransaction(TransactionRecord transactionRecord) {
        Transaction transaction = Transaction.builder()
                .type(transactionRecord.type())
                .sale(
                        transactionRecord.saleId() != null
                                ? saleRepository.getReferenceById(transactionRecord.saleId())
                                : null)
                .amount(transactionRecord.amount())
                .build();
        return transactionMapper.entityToRecord(transactionRepository.saveAndFlush(transaction));
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionRecord getTransaction(UUID transactionId) {
        return transactionRepository
                .getTransactionProjectionById(transactionId)
                .map(transactionMapper::projectionToRecord)
                .orElseThrow(() -> new NotFoundException("Transaction not found with id: " + transactionId.toString()));
    }

    @Override
    public TransactionRecord reverseTransaction(UUID originalTransactionId) {
        Transaction originalTransaction = transactionRepository
                .findById(originalTransactionId)
                .orElseThrow(() ->
                        new NotFoundException("Transaction not found with id: " + originalTransactionId));

        TransactionType reverseType =
                originalTransaction.getType() == TransactionType.PAID ? TransactionType.RECEIVED : TransactionType.PAID;

        Transaction reversal = Transaction.builder()
                .type(reverseType)
                .amount(originalTransaction.getAmount())
                .sale(originalTransaction.getSale())
                .build();
        return transactionMapper.entityToRecord(transactionRepository.saveAndFlush(reversal));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionRecord> getAllTransactions(Pageable pageable) {
        return transactionRepository.findAllBy(pageable).map(transactionMapper::projectionToRecord);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionRecord> getTransactionsByType(TransactionType type, Pageable pageable) {
        return transactionRepository.findByType(type, pageable).map(transactionMapper::projectionToRecord);
    }
}
