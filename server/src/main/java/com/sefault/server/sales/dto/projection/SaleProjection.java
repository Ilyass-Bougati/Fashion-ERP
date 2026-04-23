package com.sefault.server.sales.dto.projection;

import com.sefault.server.finance.entity.Transaction;
import com.sefault.server.hr.entity.Employee;
import com.sefault.server.sales.entity.SaleLine;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface SaleProjection {
    UUID getId();

    Double getDiscount();

    List<Transaction> getTransactions();

    List<SaleLine> getSaleLines();

    Employee getEmployee();

    Boolean getRefunded();

    LocalDateTime getCreatedAt();

    LocalDateTime getUpdatedAt();
}
