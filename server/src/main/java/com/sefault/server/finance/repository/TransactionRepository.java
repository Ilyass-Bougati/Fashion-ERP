package com.sefault.server.finance.repository;

import com.sefault.server.finance.dto.projection.TransactionProjection;
import com.sefault.server.finance.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import lombok.NonNull;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<@NonNull Transaction, @NonNull UUID> {
    Optional<TransactionProjection> getTransactionProjectionById(UUID id);
}