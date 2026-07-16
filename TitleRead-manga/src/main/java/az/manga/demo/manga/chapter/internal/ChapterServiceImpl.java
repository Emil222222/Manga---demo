package az.manga.demo.manga.chapter.internal;

import az.manga.demo.manga.chapter.api.ChapterService;
import az.manga.demo.manga.chapter.internal.dto.ChapterCreateDto;
import az.manga.demo.manga.chapter.api.dto.ChapterDto;
import az.manga.demo.manga.chapter.api.dto.ChapterListDto;
import az.manga.demo.common.event.ChapterReadEvent;
import az.manga.demo.common.exception.ConflictException;
import az.manga.demo.common.exception.ErrorMessage;
import az.manga.demo.common.exception.NotFoundException;
import az.manga.demo.manga.page.api.PageService;
import az.manga.demo.manga.page.api.dto.PageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ChapterServiceImpl implements ChapterService {
    private final ChapterRepository chapterRepository;
    private final ChapterMapper chapterMapper;
    private final PageService pageService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Cacheable(value = "chapters", key = "#id")
    @Transactional(readOnly = true)
    public ChapterDto getChapter(Long id, Long userId) {
        log.debug("Fetching chapter with id = {}", id);
        Chapter chapter = chapterRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.CHAPTER_NOT_FOUND));

        ChapterDto chapterDto = buildChapterDto(chapter);

        if (userId != null) {
            eventPublisher.publishEvent(new ChapterReadEvent(
                    chapterDto.getMangaId(), id, userId, 1
            ));
        }

        return chapterDto;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ChapterListDto> getChaptersByManga(Long mangaId, Pageable pageable){
        log.debug("Fetching chapters for manga id = {}", mangaId);
        return chapterRepository.findByMangaIdOrderByChapterNumberAsc(mangaId, pageable)
                .map(chapterMapper::toListDto);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "chapters", key = "#result.id"),
            @CacheEvict(value = "chapters:count", key = "#result.mangaId")
    })
    public ChapterDto createChapter(ChapterCreateDto chapterDto){
        log.debug("Creating chapter for manga id = {}", chapterDto.getMangaId());
        if (chapterRepository.findByMangaIdAndChapterNumber(
                chapterDto.getMangaId(),
                chapterDto.getChapterNumber()).isPresent()) {
            throw new ConflictException(ErrorMessage.CHAPTER_ALREADY_EXISTS);
        }
        Chapter chapter = chapterMapper.toEntity(chapterDto);
        chapter.setMangaId(chapterDto.getMangaId());

        List<String> pageUrls = chapterDto.getPageImageUrls();
        boolean hasPages = pageUrls != null && !pageUrls.isEmpty();

        if (hasPages) {
            chapter.setPageCount(pageUrls.size());
        }

        Chapter saved = chapterRepository.save(chapter);

        if (hasPages) {
            pageService.addPages(saved.getId(), pageUrls);
        }

        return buildChapterDto(saved);
    }

    @Override
    @CacheEvict(value = "chapters", key = "#id")
    public ChapterDto updateChapter(Long id, ChapterCreateDto request) {
        log.debug("Updating chapter id = {}", id);
        Chapter chapter = chapterRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.CHAPTER_NOT_FOUND));

        chapter.setTitle(request.getTitle());
        chapter.setChapterNumber(request.getChapterNumber());
        chapter.setVolumeNumber(request.getVolumeNumber());
        return chapterMapper.toDto(chapterRepository.save(chapter));
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "chapters", key = "#id"),
            @CacheEvict(value = "chapters:count", allEntries = true)
    })
    public void deleteChapter(Long id) {
        log.debug("Deleting chapter id = {}", id);
        if (!chapterRepository.existsById(id)) {
            throw new NotFoundException(ErrorMessage.CHAPTER_NOT_FOUND);
        }
        chapterRepository.deleteById(id);
    }

    @Override
    @Cacheable(value = "chapters:count", key = "#mangaId")
    @Transactional(readOnly = true)
    public long getChapterCount(Long mangaId) {
        log.debug("Counting chapters for manga id = {}", mangaId);
        return chapterRepository.countByMangaId(mangaId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChapterListDto> getChapterListDtoByIds(Set<Long> ids) {
        return chapterRepository.findAllById(ids).stream()
                .map(chapterMapper::toListDto)
                .toList();
    }

    private ChapterDto buildChapterDto(Chapter chapter) {
        ChapterDto chapterDto = chapterMapper.toDto(chapter);

        List<PageDto> pages = pageService.getPagesByChapter(chapter.getId());
        chapterDto.setPages(pages);
        chapterDto.setPageCount(pages.size());

        chapterRepository.findByMangaIdAndChapterNumber(chapter.getMangaId(), chapter.getChapterNumber() - 1)
                .ifPresent(prev -> chapterDto.setPreviousChapterNumber(prev.getChapterNumber()));
        chapterRepository.findByMangaIdAndChapterNumber(chapter.getMangaId(), chapter.getChapterNumber() + 1)
                .ifPresent(next -> chapterDto.setNextChapterNumber(next.getChapterNumber()));

        return chapterDto;
    }

}
