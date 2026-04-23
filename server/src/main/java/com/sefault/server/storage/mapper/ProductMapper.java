package com.sefault.server.storage.mapper;

import com.sefault.server.storage.dto.projection.ProductProjection;
import com.sefault.server.storage.dto.record.ProductRecord;
import com.sefault.server.storage.entity.Product;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    ProductRecord entityToRecord(Product Product);

    ProductRecord projectionToRecord(ProductProjection projection);

    Product toEntity(ProductRecord record);

    void updateEntityFromRecord(ProductRecord record, @MappingTarget Product entityToUpdate);
}
