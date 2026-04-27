package com.sefault.server.user.mapper;

import com.sefault.server.user.dto.projection.AuthorityProjection;
import com.sefault.server.user.dto.projection.ReportCategoryProjection;
import com.sefault.server.user.dto.record.AuthorityRecord;
import com.sefault.server.user.dto.record.ReportCategoryRecord;
import com.sefault.server.user.entity.Authority;
import com.sefault.server.user.entity.ReportCategory;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AuthorityMapper {
    AuthorityRecord entityToRecord(Authority ReportCategory);

    AuthorityRecord projectionToRecord(AuthorityProjection projection);

    Authority toEntity(AuthorityRecord record);

    void updateEntityFromRecord(AuthorityRecord record, @MappingTarget Authority entityToUpdate);
}
