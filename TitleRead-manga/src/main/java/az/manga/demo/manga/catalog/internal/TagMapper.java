package az.manga.demo.manga.catalog.internal;

import az.manga.demo.manga.catalog.api.dto.TagDto;
import az.manga.demo.manga.catalog.internal.dto.TagCreateDto;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface TagMapper {

    TagDto toDto(Tag tag);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Tag toEntity(TagCreateDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntity(@MappingTarget Tag tag, TagCreateDto dto);
}