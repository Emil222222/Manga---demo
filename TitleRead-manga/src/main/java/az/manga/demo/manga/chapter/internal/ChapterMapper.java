package az.manga.demo.manga.chapter.internal;


import az.manga.demo.manga.chapter.internal.dto.ChapterCreateDto;
import az.manga.demo.manga.chapter.api.dto.ChapterDto;
import az.manga.demo.manga.chapter.api.dto.ChapterListDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ChapterMapper {

    @Mapping(target = "mangaSlug", ignore = true)
    @Mapping(target = "pages", ignore = true)
    @Mapping(target = "previousChapterNumber", ignore = true)
    @Mapping(target = "nextChapterNumber", ignore = true)
    ChapterDto toDto(Chapter chapter);

    ChapterListDto toListDto(Chapter chapter);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "publishedAt", ignore = true)
    @Mapping(target = "pageCount", constant = "0")
    Chapter toEntity(ChapterCreateDto request);
}
