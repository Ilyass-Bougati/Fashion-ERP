package com.sefault.server.hr.mapper;

import com.sefault.server.hr.dto.projection.IsleProjection;
import com.sefault.server.hr.dto.record.IsleRecord;
import com.sefault.server.hr.entity.Isle;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface IsleMapper {
    IsleRecord entityToRecord(Isle Isle);

    IsleRecord projectionToRecord(IsleProjection projection);

    Isle toEntity(IsleRecord record);

    void updateEntityFromRecord(IsleRecord record, @MappingTarget Isle entityToUpdate);
}
