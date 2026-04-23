package com.sefault.server.user.mapper;

import com.sefault.server.user.dto.projection.SessionProjection;
import com.sefault.server.user.dto.record.SessionRecord;
import com.sefault.server.user.entity.Session;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SessionMapper {
    SessionRecord entityToRecord(Session Session);

    SessionRecord projectionToRecord(SessionProjection projection);

    Session toEntity(SessionRecord record);

    void updateEntityFromRecord(SessionRecord record, @MappingTarget Session entityToUpdate);
}
