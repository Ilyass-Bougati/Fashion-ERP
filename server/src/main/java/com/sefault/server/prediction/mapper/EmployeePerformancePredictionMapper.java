package com.sefault.server.prediction.mapper;

import com.sefault.server.prediction.dto.record.EmployeePerformancePredictionRecord;
import com.sefault.server.prediction.entity.EmployeePerformancePrediction;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface EmployeePerformancePredictionMapper {

    EmployeePerformancePredictionRecord entityToRecord(EmployeePerformancePrediction entity);

    EmployeePerformancePrediction toEntity(EmployeePerformancePredictionRecord record);

    void updateEntityFromRecord(
            EmployeePerformancePredictionRecord record, @MappingTarget EmployeePerformancePrediction entityToUpdate);
}