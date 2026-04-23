package com.sefault.server.hr.dto.record;

import com.sefault.server.finance.entity.Payroll;
import com.sefault.server.hr.entity.Isle;
import com.sefault.server.image.entity.Image;
import com.sefault.server.sales.entity.Sale;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record EmployeeRecord(
        UUID id,
        Image image,
        List<Payroll> payrolls,
        List<Sale> sales,
        List<Isle> isles,
        String firstName,
        String lastName,
        String phoneNumber,
        String CIN,
        String email,
        Boolean active,
        Double salary,
        Double commission,
        LocalDateTime hiredAt,
        LocalDateTime terminatedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {}
