package az.manga.demo.manga.manga.api.dto;

import az.manga.demo.manga.catalog.api.dto.GenreDto;
import az.manga.demo.manga.catalog.api.dto.TagDto;
import az.manga.demo.manga.chapter.api.dto.ChapterListDto;
import az.manga.demo.common.enums.MangaStatus;
import az.manga.demo.common.enums.MangaType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MangaDto {
    private Long id;

    private String title;

    private String slug;

    private String description;

    private String author;

    private String artist;

    private MangaStatus status;

    private MangaType type;

    private String coverImageUrl;

    private Integer totalChapters;

    private Double averageRating;

    private Integer viewCount;

    private Set<GenreDto> genres;
    private Set<TagDto> tags;

    private List<ChapterListDto> recentChapters;

    private Long favoriteCount;
    private Integer ratingCount;
}
