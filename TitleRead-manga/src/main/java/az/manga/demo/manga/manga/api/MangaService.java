package az.manga.demo.manga.manga.api;

import az.manga.demo.manga.manga.api.dto.MangaDto;
import az.manga.demo.manga.manga.api.dto.MangaListDto;
import az.manga.demo.manga.manga.internal.dto.MangaCreateDto;
import az.manga.demo.manga.manga.internal.dto.MangaFilterDto;
import az.manga.demo.manga.manga.internal.dto.MangaUpdateDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

public interface MangaService {
    MangaDto create(MangaCreateDto dto);
    MangaDto getById(Long id);
    MangaDto getBySlug(String slug);
    Page<MangaListDto> getAll(Pageable pageable);
    Page<MangaListDto> filter(MangaFilterDto filterDto, Pageable pageable);
    MangaDto update(Long id, MangaUpdateDto dto);
    void delete(Long id);
    void registerView(Long id, Long userId);
    MangaListDto getMangaListById(Long id);
    List<MangaListDto> getMangaListByIds(Set<Long> ids);
}
