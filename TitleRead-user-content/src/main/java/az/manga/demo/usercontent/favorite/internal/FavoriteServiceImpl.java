package az.manga.demo.usercontent.favorite.internal;

import az.manga.demo.common.exception.ConflictException;
import az.manga.demo.common.exception.ErrorMessage;
import az.manga.demo.common.exception.NotFoundException;
import az.manga.demo.usercontent.favorite.api.FavoriteService;
import az.manga.demo.usercontent.favorite.api.dto.FavoriteDto;
import az.manga.demo.usercontent.favorite.internal.dto.FavoriteCreateDto;
import az.manga.demo.manga.manga.api.MangaService;
import az.manga.demo.manga.manga.api.dto.MangaListDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final FavoriteMapper favoriteMapper;
    private final MangaService mangaService;

    @Override
    @CacheEvict(value = "favorites:count", key = "#dto.mangaId")
    public FavoriteDto add(Long userId, FavoriteCreateDto dto) {
        log.info("Adding manga id = {} to favorites for user id = {}", dto.getMangaId(), userId);
        if(favoriteRepository.existsByUserIdAndMangaId(userId, dto.getMangaId())){
            throw new ConflictException(ErrorMessage.ALREADY_IN_FAVORITES);
        }

        FavoriteDto saved = favoriteMapper.toDto(favoriteRepository.save(UserFavorite.builder()
                .userId(userId)
                .mangaId(dto.getMangaId())
                .build()));
        saved.setMangaListDto(mangaService.getMangaListById(dto.getMangaId()));

        return saved;
    }

    @Override
    @CacheEvict(value = "favorites:count", key = "#mangaId")
    public void remove(Long userId, Long mangaId) {
        log.info("Removing manga id = {} to favorites for user id = {}", mangaId, userId);
        if (!favoriteRepository.existsByUserIdAndMangaId(userId, mangaId)) {
            throw new NotFoundException(ErrorMessage.FAVORITE_NOT_FOUND);
        }
        favoriteRepository.deleteByUserIdAndMangaId(userId, mangaId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FavoriteDto> getUserFavorites(Long userId, Pageable pageable) {
        Page<UserFavorite> favorites = favoriteRepository.findByUserIdOrderByAddedAtDesc(userId, pageable);

        Set<Long> mangaIds = favorites.stream()
                .map(UserFavorite::getMangaId)
                .collect(Collectors.toSet());

        Map<Long, MangaListDto> mangaListDtos = mangaService.getMangaListByIds(mangaIds).stream()
                .collect(Collectors.toMap(MangaListDto::getId, manga -> manga));

        return favorites.map(favorite -> {
            FavoriteDto dto = favoriteMapper.toDto(favorite);
            dto.setMangaListDto(mangaListDtos.get(favorite.getMangaId()));
            return dto;
        });
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFavorite(Long userId, Long mangaId) {
        return favoriteRepository.existsByUserIdAndMangaId(userId, mangaId);
    }

    @Override
    @Cacheable(value = "favorites:count", key = "#mangaId")
    @Transactional(readOnly = true)
    public long countByManga(Long mangaId) {
        return favoriteRepository.countByMangaId(mangaId);
    }
}
