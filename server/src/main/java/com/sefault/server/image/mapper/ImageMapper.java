package com.sefault.server.image.mapper;

import com.sefault.server.image.dto.projection.ImageProjection;
import com.sefault.server.image.dto.record.ImageRecord;
import com.sefault.server.image.entity.Image;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ImageMapper {
    ImageRecord entityToRecord(Image Image);

    ImageRecord projectionToRecord(ImageProjection projection);

    Image toEntity(ImageRecord record);

    void updateEntityFromRecord(ImageRecord record, @MappingTarget Image entityToUpdate);
}
