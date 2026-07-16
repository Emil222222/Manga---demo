package az.manga.demo.manga.catalog.internal;

import az.manga.demo.manga.catalog.api.TagService;
import az.manga.demo.manga.catalog.api.dto.TagDto;
import az.manga.demo.manga.catalog.internal.dto.TagCreateDto;
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
@Slf4j
@RequiredArgsConstructor
@Transactional
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;
    private final TagMapper tagMapper;

    @Override
    @CacheEvict(value = "tags", allEntries = true)
    public TagDto create(TagCreateDto dto) {
        log.info("Creating tag {}", dto);
        if (tagRepository.existsBySlug(dto.getSlug())){
            throw new ConflictException(ErrorMessage.TAG_ALREADY_EXISTS);
        }
        return tagMapper.toDto(tagRepository.save(tagMapper.toEntity(dto)));
    }

    @Override
    @Cacheable(value = "tags", key = "#id")
    @Transactional(readOnly = true)
    public TagDto getById(Long id) {
        log.debug("Fetching tag {}", id);
        return tagMapper.toDto(findById(id));
    }

    @Override
    @Cacheable(value = "tags:all")
    @Transactional(readOnly = true)
    public List<TagDto> getAll() {
        log.debug("Fetching all tags");
        return tagRepository.findAllByOrderByNameAsc()
                .stream()
                .map(tagMapper::toDto)
                .toList();
    }

    @Override
    @CacheEvict(value = "tags", allEntries = true)
    public TagDto update(Long id, TagCreateDto dto) {
        log.info("Updating tag {}", id);
        Tag tag = findById(id);
        tagMapper.updateEntity(tag, dto);
        return tagMapper.toDto(tagRepository.save(tag));
    }

    @Override
    @CacheEvict(value = "tags", allEntries = true)
    public void delete(Long id) {
        log.info("Deleting tag {}", id);
        if (!tagRepository.existsById(id)) {
            throw new NotFoundException(ErrorMessage.TAG_NOT_FOUND);
        }
        tagRepository.deleteById(id);
    }

    private Tag findById(Long id) {
        return tagRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.TAG_NOT_FOUND));
    }
}
