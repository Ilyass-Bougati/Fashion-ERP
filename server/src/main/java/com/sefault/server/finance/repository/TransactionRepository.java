package com.sefault.server.finance.repository;

import com.sefault.server.finance.dto.projection.TransactionProjection;
import com.sefault.server.finance.entity.Transaction;
import java.util.Optional;
import java.util.UUID;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<@NonNull Transaction, @NonNull UUID> {
    Optional<TransactionProjection> getTransactionProjectionById(UUID id);
}
