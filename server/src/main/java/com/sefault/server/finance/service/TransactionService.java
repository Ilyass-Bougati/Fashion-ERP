package com.sefault.server.finance.service;

import com.sefault.server.finance.dto.record.TransactionRecord;
import com.sefault.server.finance.enums.TransactionType;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TransactionService {
    TransactionRecord createTransaction(TransactionRecord transactionRecord);

    TransactionRecord getTransaction(UUID transactionId);

    TransactionRecord reverseTransaction(UUID originalTransactionId);

    Page<TransactionRecord> getAllTransactions(Pageable pageable);

    Page<TransactionRecord> getTransactionsByType(TransactionType type, Pageable pageable);
}
