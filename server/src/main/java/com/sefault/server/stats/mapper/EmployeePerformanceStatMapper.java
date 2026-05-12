package com.sefault.server.stats.mapper;

import com.sefault.server.stats.dto.projection.EmployeePerformanceStatProjection;
import com.sefault.server.stats.dto.record.EmployeePerformanceStatRecord;
import com.sefault.server.stats.entity.EmployeePerformanceStat;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface EmployeePerformanceStatMapper {
    EmployeePerformanceStatRecord entityToRecord(EmployeePerformanceStat stat);

    EmployeePerformanceStatRecord projectionToRecord(EmployeePerformanceStatProjection projection);

    EmployeePerformanceStat toEntity(EmployeePerformanceStatRecord record);

    void updateEntityFromRecord(
            EmployeePerformanceStatRecord record, @MappingTarget EmployeePerformanceStat entityToUpdate);
}
