package az.manga.demo.manga.catalog.api;

import az.manga.demo.manga.catalog.api.dto.TagDto;
import az.manga.demo.manga.catalog.internal.dto.TagCreateDto;

import java.util.List;

public interface TagService {
    TagDto create(TagCreateDto dto);
    TagDto getById(Long id);
    List<TagDto> getAll();
    TagDto update(Long id, TagCreateDto dto);
    void delete(Long id);
}