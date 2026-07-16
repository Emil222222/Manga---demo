package az.manga.demo.manga.page.api;

import az.manga.demo.manga.page.api.dto.PageDto;
import az.manga.demo.manga.page.internal.dto.PageCreateDto;

import java.util.List;

public interface PageService {
    List<PageDto> getPagesByChapter(Long chapterId);
    PageDto addPage(PageCreateDto pageCreateDto);
    PageDto updatePage(Long id, PageCreateDto pageCreateDto);
    void deletePage(Long id);
    void addPages(Long chapterId, List<String> imageUrls);
}
