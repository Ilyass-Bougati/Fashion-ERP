package com.sefault.server.storage.mapper;

import com.sefault.server.storage.dto.projection.ProductCategoryProjection;
import com.sefault.server.storage.dto.record.ProductCategoryRecord;
import com.sefault.server.storage.entity.ProductCategory;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProductCategoryMapper {
    ProductCategoryRecord entityToRecord(ProductCategory ProductCategory);

    ProductCategoryRecord projectionToRecord(ProductCategoryProjection projection);

    ProductCategory toEntity(ProductCategoryRecord record);

    void updateEntityFromRecord(ProductCategoryRecord record, @MappingTarget ProductCategory entityToUpdate);
}
