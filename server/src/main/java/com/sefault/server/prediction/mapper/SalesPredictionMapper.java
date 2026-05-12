package com.sefault.server.prediction.mapper;

import com.sefault.server.prediction.dto.record.SalesPredictionRecord;
import com.sefault.server.prediction.entity.SalesPrediction;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SalesPredictionMapper {

    SalesPredictionRecord entityToRecord(SalesPrediction entity);

    SalesPrediction toEntity(SalesPredictionRecord record);

    void updateEntityFromRecord(SalesPredictionRecord record, @MappingTarget SalesPrediction entityToUpdate);
}
