package az.manga.demo.usercontent.favorite.api;

import az.manga.demo.usercontent.favorite.api.dto.FavoriteDto;
import az.manga.demo.usercontent.favorite.internal.dto.FavoriteCreateDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FavoriteService {
    FavoriteDto add(Long userId, FavoriteCreateDto dto);
    void remove(Long userId, Long mangaId);
    Page<FavoriteDto> getUserFavorites(Long userId, Pageable pageable);
    boolean isFavorite(Long userId, Long mangaId);
    long countByManga(Long mangaId);
}
