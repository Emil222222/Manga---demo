package az.manga.demo.manga.chapter.api.dto;

import az.manga.demo.manga.page.api.dto.PageDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChapterDto {

    private Long id;

    private Long mangaId;

    private String mangaTitle; // для breadcrumbs

    private String mangaSlug;

    private String title;

    private Double chapterNumber;

    private Integer volumeNumber;

    private Integer pageCount;

    private List<PageDto> pages;

    private Double previousChapterNumber;

    private Double nextChapterNumber;
}
