package az.manga.demo.manga.manga.internal;

import az.manga.demo.manga.manga.api.dto.MangaDto;
import az.manga.demo.manga.manga.api.dto.MangaListDto;
import az.manga.demo.manga.manga.internal.dto.MangaCreateDto;
import az.manga.demo.manga.manga.internal.dto.MangaUpdateDto;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MangaMapper {

    @Mapping(target = "genres", source = "genres")
    @Mapping(target = "tags", source = "tags")
    @Mapping(target = "recentChapters", ignore = true)
    @Mapping(target = "favoriteCount", ignore = true)
    @Mapping(target = "ratingCount", ignore = true)
    MangaDto toDto(Manga manga);

    @Mapping(target = "genreNames", expression = "java(manga.getGenres().stream().map(g -> g.getName()).collect(java.util.stream.Collectors.toSet()))")
    @Mapping(target = "latestChapterNumber", ignore = true)
    MangaListDto toListDto(Manga manga);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "genres", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "totalChapters", constant = "0")
    @Mapping(target = "averageRating", constant = "0.0")
    @Mapping(target = "viewCount", constant = "0")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "status", expression = "java(az.manga.demo.common.enums.MangaStatus.valueOf(dto.getStatus()))")
    @Mapping(target = "type", expression = "java(az.manga.demo.common.enums.MangaType.valueOf(dto.getType()))")
    Manga toEntity(MangaCreateDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "genres", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "totalChapters", ignore = true)
    @Mapping(target = "averageRating", ignore = true)
    @Mapping(target = "viewCount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "status", expression = "java(dto.getStatus() != null ? az.manga.demo.common.enums.MangaStatus.valueOf(dto.getStatus()) : manga.getStatus())")
    @Mapping(target = "type", expression = "java(dto.getType() != null ? az.manga.demo.common.enums.MangaType.valueOf(dto.getType()) : manga.getType())")
    void updateEntity(@MappingTarget Manga manga, MangaUpdateDto dto);
}

