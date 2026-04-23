package com.sefault.server.user.mapper;

import com.sefault.server.user.dto.projection.ReportCategoryProjection;
import com.sefault.server.user.dto.record.ReportCategoryRecord;
import com.sefault.server.user.entity.ReportCategory;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ReportCategoryMapper {
    ReportCategoryRecord entityToRecord(ReportCategory ReportCategory);

    ReportCategoryRecord projectionToRecord(ReportCategoryProjection projection);

    ReportCategory toEntity(ReportCategoryRecord record);

    void updateEntityFromRecord(ReportCategoryRecord record, @MappingTarget ReportCategory entityToUpdate);
}
