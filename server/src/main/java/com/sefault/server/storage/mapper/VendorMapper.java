package com.sefault.server.storage.mapper;

import com.sefault.server.storage.dto.projection.VendorProjection;
import com.sefault.server.storage.dto.record.VendorRecord;
import com.sefault.server.storage.entity.Vendor;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface VendorMapper {
    VendorRecord entityToRecord(Vendor Vendor);

    VendorRecord projectionToRecord(VendorProjection projection);

    Vendor toEntity(VendorRecord record);

    void updateEntityFromRecord(VendorRecord record, @MappingTarget Vendor entityToUpdate);
}
