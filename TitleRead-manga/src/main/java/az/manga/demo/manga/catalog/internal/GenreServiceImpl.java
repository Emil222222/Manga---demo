package az.manga.demo.manga.catalog.internal;

import az.manga.demo.manga.catalog.api.GenreService;
import az.manga.demo.manga.catalog.api.dto.GenreDto;
import az.manga.demo.manga.catalog.internal.dto.GenreCreateDto;
import az.manga.demo.common.exception.ConflictException;
import az.manga.demo.common.exception.ErrorMessage;
import az.manga.demo.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class GenreServiceImpl implements GenreService {

    private final GenreRepository genreRepository;
    private final GenreMapper genreMapper;

    @Override
    @CacheEvict(value = "genres", allEntries = true)
    public GenreDto create(GenreCreateDto dto) {
        log.info("Creating Genre {} ", dto);
        if (genreRepository.existsBySlug(dto.getSlug())) {
            throw new ConflictException(ErrorMessage.GENRE_ALREADY_EXISTS);
        }
        return genreMapper.toDto(genreRepository.save(genreMapper.toEntity(dto)));
    }

    @Override
    @Cacheable(value = "genres", key = "#id")
    @Transactional(readOnly = true)
    public GenreDto getById(Long id) {
        log.debug("Fetching Genre with id {} ", id);
        return genreMapper.toDto(findById(id));
    }

    @Override
    @Cacheable(value = "genres:all")
    @Transactional(readOnly = true)
    public List<GenreDto> getAll() {
        log.debug("Fetching all Genre");
        return genreRepository.findAllByOrderByNameAsc()
                .stream()
                .map(genreMapper::toDto)
                .toList();
    }

    @Override
    @CacheEvict(value = "genres", allEntries = true)
    public GenreDto update(Long id, GenreCreateDto dto) {
        log.info("Updating Genre with id {} ", id);
        Genre genre = findById(id);
        genreMapper.updateEntity(genre, dto);
        return genreMapper.toDto(genreRepository.save(genre));
    }

    @Override
    @CacheEvict(value = "genres", allEntries = true)
    public void delete(Long id) {
        log.info("Deleting Genre with id {} ", id);
        if (!genreRepository.existsById(id)) {
            throw new NotFoundException(ErrorMessage.GENRE_NOT_FOUND);
        }
        genreRepository.deleteById(id);
    }

    private Genre findById(Long id) {
        return genreRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.GENRE_NOT_FOUND));
    }
}
