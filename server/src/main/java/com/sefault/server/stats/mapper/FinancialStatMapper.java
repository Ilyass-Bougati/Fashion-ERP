package com.sefault.server.stats.mapper;

import com.sefault.server.stats.dto.projection.FinancialStatProjection;
import com.sefault.server.stats.dto.record.FinancialStatRecord;
import com.sefault.server.stats.entity.FinancialStat;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface FinancialStatMapper {
    FinancialStatRecord entityToRecord(FinancialStat stat);

    FinancialStatRecord projectionToRecord(FinancialStatProjection projection);

    FinancialStat toEntity(FinancialStatRecord record);

    void updateEntityFromRecord(FinancialStatRecord record, @MappingTarget FinancialStat entityToUpdate);
}
