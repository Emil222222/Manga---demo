package az.manga.demo.manga.catalog.internal;

import az.manga.demo.manga.catalog.api.dto.GenreDto;
import az.manga.demo.manga.catalog.internal.dto.GenreCreateDto;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface GenreMapper {

    GenreDto toDto(Genre genre);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Genre toEntity(GenreCreateDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntity(@MappingTarget Genre genre,  GenreCreateDto dto);
}
