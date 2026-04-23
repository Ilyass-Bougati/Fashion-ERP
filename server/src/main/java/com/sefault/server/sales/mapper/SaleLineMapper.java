package com.sefault.server.sales.mapper;

import com.sefault.server.sales.dto.projection.SaleLineProjection;
import com.sefault.server.sales.dto.record.SaleLineRecord;
import com.sefault.server.sales.entity.SaleLine;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SaleLineMapper {
    SaleLineRecord entityToRecord(SaleLine SaleLine);

    SaleLineRecord projectionToRecord(SaleLineProjection projection);

    SaleLine toEntity(SaleLineRecord record);

    void updateEntityFromRecord(SaleLineRecord record, @MappingTarget SaleLine entityToUpdate);
}
