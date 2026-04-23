package com.sefault.server.user.mapper;

import com.sefault.server.user.dto.projection.UserReportProjection;
import com.sefault.server.user.dto.record.UserReportRecord;
import com.sefault.server.user.entity.UserReport;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserReportMapper {
    UserReportRecord entityToRecord(UserReport UserReport);

    UserReportRecord projectionToRecord(UserReportProjection projection);

    UserReport toEntity(UserReportRecord record);

    void updateEntityFromRecord(UserReportRecord record, @MappingTarget UserReport entityToUpdate);
}
