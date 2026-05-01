package com.sefault.server.finance.repository;

import com.sefault.server.finance.dto.projection.TransactionProjection;
import com.sefault.server.finance.entity.Transaction;
import com.sefault.server.finance.enums.TransactionType;
import java.util.Optional;
import java.util.UUID;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<@NonNull Transaction, @NonNull UUID> {
    Optional<TransactionProjection> getTransactionProjectionById(UUID id);

    Page<TransactionProjection> findAllBy(Pageable pageable);

    Page<TransactionProjection> findByType(TransactionType type, Pageable pageable);
}
