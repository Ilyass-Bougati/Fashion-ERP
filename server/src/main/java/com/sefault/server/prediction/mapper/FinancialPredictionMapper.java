package com.sefault.server.prediction.mapper;

import com.sefault.server.prediction.dto.record.FinancialPredictionRecord;
import com.sefault.server.prediction.entity.FinancialPrediction;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface FinancialPredictionMapper {

    FinancialPredictionRecord entityToRecord(FinancialPrediction entity);

    FinancialPrediction toEntity(FinancialPredictionRecord record);

    void updateEntityFromRecord(FinancialPredictionRecord record, @MappingTarget FinancialPrediction entityToUpdate);
}
