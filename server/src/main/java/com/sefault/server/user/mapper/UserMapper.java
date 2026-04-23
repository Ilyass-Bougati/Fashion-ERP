package com.sefault.server.user.mapper;

import com.sefault.server.user.dto.projection.UserProjection;
import com.sefault.server.user.dto.record.UserRecord;
import com.sefault.server.user.entity.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserRecord entityToRecord(User User);

    UserRecord projectionToRecord(UserProjection projection);

    User toEntity(UserRecord record);

    void updateEntityFromRecord(UserRecord record, @MappingTarget User entityToUpdate);
}
