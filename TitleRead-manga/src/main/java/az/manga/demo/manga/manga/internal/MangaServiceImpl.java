package az.manga.demo.manga.manga.internal;


import az.manga.demo.common.event.MangaViewedEvent;
import az.manga.demo.common.exception.ErrorMessage;
import az.manga.demo.common.exception.NotFoundException;

import az.manga.demo.manga.catalog.internal.Genre;
import az.manga.demo.manga.catalog.internal.Tag;
import az.manga.demo.manga.catalog.internal.GenreRepository;
import az.manga.demo.manga.catalog.internal.TagRepository;
import az.manga.demo.manga.manga.api.MangaService;
import az.manga.demo.manga.manga.api.dto.MangaDto;
import az.manga.demo.manga.manga.api.dto.MangaListDto;
import az.manga.demo.manga.manga.internal.dto.MangaCreateDto;
import az.manga.demo.manga.manga.internal.dto.MangaFilterDto;
import az.manga.demo.manga.manga.internal.dto.MangaUpdateDto;
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class MangaServiceImpl implements MangaService {

    private final MangaRepository mangaRepository;
    private final MangaMapper mangaMapper;
    private final GenreRepository genreRepository;
    private final TagRepository tagRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public MangaDto create(MangaCreateDto dto) {
        log.info("Creating manga {}", dto);
        Manga manga = mangaMapper.toEntity(dto);
        resolveAndSetGenres(manga, dto.getGenreIds());
        resolveAndSetTags(manga, dto.getTagIds());
        return mangaMapper.toDto(mangaRepository.save(manga));
    }

    @Override
    @Cacheable(value = "manga", key = "#id")
    @Transactional(readOnly = true)
    public MangaDto getById(Long id) {
        log.debug("Fetching manga by id = {}", id);
        return mangaMapper.toDto(findById(id));
    }

    @Override
    @Cacheable(value = "manga", key =  "'slug:' + #slug")
    @Transactional(readOnly = true)
    public MangaDto getBySlug(String slug) {
        log.debug("Fetching manga by slug = {}", slug);
        return mangaMapper.toDto(mangaRepository.findBySlug(slug)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.MANGA_NOT_FOUND)));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MangaListDto> getAll(Pageable pageable) {
        log.debug("Fetching all manga, page = {}", pageable);
        return mangaRepository.findAll(pageable)
                .map(mangaMapper::toListDto);

    }

    @Override
    @Transactional(readOnly = true)
    public Page<MangaListDto> filter(MangaFilterDto filterDto, Pageable pageable) {
        log.debug("Fetching all manga, filter = {}", filterDto);
        return mangaRepository.findAll(MangaSpecification.filter(filterDto),pageable)
                .map(mangaMapper::toListDto);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "manga", key = "#id"),
            @CacheEvict(value = "manga", key = "'slug:' + #result.slug")
    })
    public MangaDto update(Long id, MangaUpdateDto dto) {
        log.info("Updating manga id = {}", dto);
        Manga manga = findById(id);
        mangaMapper.updateEntity(manga, dto);
        resolveAndSetGenres(manga, dto.getGenreIds());
        resolveAndSetTags(manga, dto.getTagIds());
        return mangaMapper.toDto(mangaRepository.save(manga));
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "manga", key = "#id"),
            @CacheEvict(value = "manga", allEntries = true)
    })
    public void delete(Long id) {
        log.info("Deleting manga by id = {}", id);
        if(!mangaRepository.existsById(id)) {
            throw new NotFoundException(ErrorMessage.MANGA_NOT_FOUND);
        }
        mangaRepository.deleteById(id);
    }

    @Override
    public void registerView(Long id, Long userId){
        eventPublisher.publishEvent(new MangaViewedEvent(id, userId));
    }

    @Override
    @Transactional(readOnly = true)
    public MangaListDto getMangaListById(Long id) {
        return mangaMapper.toListDto(findById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MangaListDto> getMangaListByIds(Set<Long> ids) {
        return mangaRepository.findAllById(ids).stream()
                .map(mangaMapper::toListDto)
                .toList();
    }

    private Manga findById(Long id) {
        return mangaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.MANGA_NOT_FOUND));
    }

    private void resolveAndSetGenres(Manga manga, Set<Long> genreIds) {
        if (genreIds == null || genreIds.isEmpty()) return;
        Set<Genre> genres = new HashSet<>(genreRepository.findAllById(genreIds));
        manga.setGenres(genres);
    }

    private void resolveAndSetTags(Manga manga, Set<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) return;
        Set<Tag> tags = new HashSet<>(tagRepository.findAllById(tagIds));
        manga.setTags(tags);
    }
}
