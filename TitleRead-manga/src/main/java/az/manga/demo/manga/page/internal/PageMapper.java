package az.manga.demo.manga.page.internal;

import az.manga.demo.manga.page.internal.dto.PageCreateDto;
import az.manga.demo.manga.page.api.dto.PageDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface PageMapper {

    PageDto toDto(Page page);

    @Mapping(target = "id", ignore = true)
    Page toEntity(PageCreateDto request);
}
