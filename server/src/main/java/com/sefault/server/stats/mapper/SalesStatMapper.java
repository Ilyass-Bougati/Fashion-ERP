package com.sefault.server.stats.mapper;

import com.sefault.server.stats.dto.projection.SalesStatProjection;
import com.sefault.server.stats.dto.record.SalesStatRecord;
import com.sefault.server.stats.entity.SalesStat;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SalesStatMapper {
    SalesStatRecord entityToRecord(SalesStat salesStat);

    SalesStatRecord projectionToRecord(SalesStatProjection projection);

    SalesStat toEntity(SalesStatRecord record);

    void updateEntityFromRecord(SalesStatRecord record, @MappingTarget SalesStat entityToUpdate);
}
