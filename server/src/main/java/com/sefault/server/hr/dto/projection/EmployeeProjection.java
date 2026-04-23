package com.sefault.server.hr.dto.projection;

import com.sefault.server.finance.entity.Payroll;
import com.sefault.server.hr.entity.Isle;
import com.sefault.server.image.entity.Image;
import com.sefault.server.sales.entity.Sale;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface EmployeeProjection {
    UUID getId();

    Image getImage();

    List<Payroll> getPayrolls();

    List<Sale> getSales();

    List<Isle> getIsles();

    String getFirstName();

    String getLastName();

    String getPhoneNumber();

    String getCIN();

    String getEmail();

    Boolean getActive();

    Double getSalary();

    Double getCommission();

    LocalDateTime getHiredAt();

    LocalDateTime getTerminatedAt();

    LocalDateTime getCreatedAt();

    LocalDateTime getUpdatedAt();
}