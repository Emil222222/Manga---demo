package az.manga.demo.manga.chapter.api;


import az.manga.demo.manga.chapter.api.dto.ChapterDto;
import az.manga.demo.manga.chapter.api.dto.ChapterListDto;
import az.manga.demo.manga.chapter.internal.dto.ChapterCreateDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

public interface ChapterService {
    ChapterDto getChapter(Long id, Long userId);
    Page<ChapterListDto> getChaptersByManga(Long mangaId, Pageable pageable);
    ChapterDto createChapter(ChapterCreateDto chapterDto);
    ChapterDto updateChapter(Long id, ChapterCreateDto request);
    void deleteChapter(Long id);
    long getChapterCount(Long mangaId);
    List<ChapterListDto> getChapterListDtoByIds(Set<Long> ids);
}
