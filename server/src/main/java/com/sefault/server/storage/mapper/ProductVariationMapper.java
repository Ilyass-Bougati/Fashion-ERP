package com.sefault.server.storage.mapper;

import com.sefault.server.storage.dto.projection.ProductVariationProjection;
import com.sefault.server.storage.dto.record.ProductVariationRecord;
import com.sefault.server.storage.entity.ProductVariation;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProductVariationMapper {
    ProductVariationRecord entityToRecord(ProductVariation ProductVariation);

    ProductVariationRecord projectionToRecord(ProductVariationProjection projection);

    ProductVariation toEntity(ProductVariationRecord record);

    void updateEntityFromRecord(ProductVariationRecord record, @MappingTarget ProductVariation entityToUpdate);
}
