package com.sefault.server.prediction.mapper;

import com.sefault.server.prediction.dto.record.StockPredictionRecord;
import com.sefault.server.prediction.entity.StockPrediction;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface StockPredictionMapper {

    StockPredictionRecord entityToRecord(StockPrediction entity);

    StockPrediction toEntity(StockPredictionRecord record);

    void updateEntityFromRecord(StockPredictionRecord record, @MappingTarget StockPrediction entityToUpdate);
}
