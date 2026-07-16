package az.manga.demo.manga.catalog.api;

import az.manga.demo.manga.catalog.api.dto.GenreDto;
import az.manga.demo.manga.catalog.internal.dto.GenreCreateDto;

import java.util.List;

public interface GenreService {
    GenreDto create(GenreCreateDto dto);
    GenreDto getById(Long id);
    List<GenreDto> getAll();
    GenreDto update(Long id, GenreCreateDto dto);
    void delete(Long id);
}
