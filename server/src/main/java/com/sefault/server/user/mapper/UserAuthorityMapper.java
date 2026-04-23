package com.sefault.server.user.mapper;

import com.sefault.server.user.dto.projection.UserAuthorityProjection;
import com.sefault.server.user.dto.record.UserAuthorityRecord;
import com.sefault.server.user.entity.UserAuthority;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserAuthorityMapper {
    UserAuthorityRecord entityToRecord(UserAuthority UserAuthority);

    UserAuthorityRecord projectionToRecord(UserAuthorityProjection projection);

    UserAuthority toEntity(UserAuthorityRecord record);

    void updateEntityFromRecord(UserAuthorityRecord record, @MappingTarget UserAuthority entityToUpdate);
}
