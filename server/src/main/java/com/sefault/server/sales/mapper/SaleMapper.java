package com.sefault.server.sales.mapper;

import com.sefault.server.sales.dto.projection.SaleProjection;
import com.sefault.server.sales.dto.record.SaleRecord;
import com.sefault.server.sales.entity.Sale;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SaleMapper {
    SaleRecord entityToRecord(Sale Sale);

    SaleRecord projectionToRecord(SaleProjection projection);

    Sale toEntity(SaleRecord record);

    void updateEntityFromRecord(SaleRecord record, @MappingTarget Sale entityToUpdate);
}
