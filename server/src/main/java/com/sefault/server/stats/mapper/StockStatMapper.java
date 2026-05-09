package com.sefault.server.stats.mapper;

import com.sefault.server.stats.dto.projection.StockStatProjection;
import com.sefault.server.stats.dto.record.StockStatRecord;
import com.sefault.server.stats.entity.StockStat;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface StockStatMapper {
    StockStatRecord entityToRecord(StockStat stockStat);

    StockStatRecord projectionToRecord(StockStatProjection projection);

    StockStat toEntity(StockStatRecord record);

    void updateEntityFromRecord(StockStatRecord record, @MappingTarget StockStat entityToUpdate);
}
