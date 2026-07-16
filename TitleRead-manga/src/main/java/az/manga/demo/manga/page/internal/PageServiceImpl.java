package az.manga.demo.manga.page.internal;

import az.manga.demo.common.exception.ErrorMessage;
import az.manga.demo.common.exception.NotFoundException;
import az.manga.demo.manga.page.api.PageService;
import az.manga.demo.manga.page.internal.dto.PageCreateDto;
import az.manga.demo.manga.page.api.dto.PageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PageServiceImpl implements PageService{
    private final PageRepository pageRepository;
    private final PageMapper pageMapper;

    @Override
    @Cacheable(value = "pages", key = "#chapterId")
    @Transactional(readOnly = true)
    public List<PageDto> getPagesByChapter(Long chapterId) {
    log.debug("Fetching pages for chapter id = {}", chapterId);
    return pageRepository.findByChapterIdOrderByPageNumberAsc(chapterId)
            .stream()
            .map(pageMapper::toDto)
            .toList();
    }

    @Override
    @CacheEvict(value = "pages", key = "#pageCreateDto.chapterId")
    public PageDto addPage(PageCreateDto pageCreateDto) {
        log.debug("Adding new page to chapter id  {}", pageCreateDto.getChapterId());
        Page page = pageMapper.toEntity(pageCreateDto);
        page.setChapterId(pageCreateDto.getChapterId());
        return pageMapper.toDto(pageRepository.save(page));
    }

    @Override
    @CacheEvict(value = "pages", key = "#pageCreateDto.chapterId")
    public PageDto updatePage(Long id, PageCreateDto pageCreateDto) {
        log.debug("Updating page id  {}", id);
        Page page = pageRepository.findById(id)
                .orElseThrow(()-> new NotFoundException(ErrorMessage.PAGE_NOT_FOUND));
        page.setPageNumber(pageCreateDto.getPageNumber());
        page.setImageUrl(pageCreateDto.getImageUrl());
        page.setWidth(pageCreateDto.getWidth());
        page.setHeight(pageCreateDto.getHeight());
        return pageMapper.toDto(pageRepository.save(page));
    }

    @Override
    @CacheEvict(value = "pages", allEntries = true)
    public void deletePage(Long id) {
        log.debug("Deleting page id  {}", id);
        if(!pageRepository.existsById(id)) {
            throw new NotFoundException(ErrorMessage.PAGE_NOT_FOUND);
        }
        pageRepository.deleteById(id);
    }

    @Override
    public void addPages(Long chapterId, List<String> imageUrls) {
        List<Page> pages = new ArrayList<>();
        for (int i = 0; i < imageUrls.size(); i++) {
            pages.add(Page.builder()
                    .chapterId(chapterId)
                    .pageNumber(i + 1)
                    .imageUrl(imageUrls.get(i))
                    .build());
        }
        pageRepository.saveAll(pages);
    }
}
