package com.sefault.server.user.mapper;

import com.sefault.server.user.dto.projection.ReportProjection;
import com.sefault.server.user.dto.record.ReportRecord;
import com.sefault.server.user.entity.Report;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ReportMapper {
    ReportRecord entityToRecord(Report Report);

    ReportRecord projectionToRecord(ReportProjection projection);

    Report toEntity(ReportRecord record);

    void updateEntityFromRecord(ReportRecord record, @MappingTarget Report entityToUpdate);
}
