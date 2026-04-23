package com.sefault.server.finance.mapper;

import com.sefault.server.finance.dto.projection.PayrollProjection;
import com.sefault.server.finance.dto.record.PayrollRecord;
import com.sefault.server.finance.entity.Payroll;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PayrollMapper {
    PayrollRecord entityToRecord(Payroll Payroll);

    PayrollRecord projectionToRecord(PayrollProjection projection);

    Payroll toEntity(PayrollRecord record);

    void updateEntityFromRecord(PayrollRecord record, @MappingTarget Payroll entityToUpdate);
}
