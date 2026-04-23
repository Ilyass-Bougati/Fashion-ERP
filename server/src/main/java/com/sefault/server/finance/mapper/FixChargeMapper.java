package com.sefault.server.finance.mapper;

import com.sefault.server.finance.dto.projection.FixChargeProjection;
import com.sefault.server.finance.dto.record.FixChargeRecord;
import com.sefault.server.finance.entity.FixCharge;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface FixChargeMapper {
    FixChargeRecord entityToRecord(FixCharge FixCharge);

    FixChargeRecord projectionToRecord(FixChargeProjection projection);

    FixCharge toEntity(FixChargeRecord record);

    void updateEntityFromRecord(FixChargeRecord record, @MappingTarget FixCharge entityToUpdate);
}
