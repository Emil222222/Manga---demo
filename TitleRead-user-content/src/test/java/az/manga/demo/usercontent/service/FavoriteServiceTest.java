package az.manga.demo.usercontent.service;

import az.manga.demo.common.exception.ConflictException;
import az.manga.demo.common.exception.NotFoundException;
import az.manga.demo.manga.manga.api.MangaService;
import az.manga.demo.manga.manga.api.dto.MangaListDto;
import az.manga.demo.usercontent.favorite.api.dto.FavoriteDto;
import az.manga.demo.usercontent.favorite.internal.FavoriteMapper;
import az.manga.demo.usercontent.favorite.internal.FavoriteRepository;
import az.manga.demo.usercontent.favorite.internal.FavoriteServiceImpl;
import az.manga.demo.usercontent.favorite.internal.UserFavorite;
import az.manga.demo.usercontent.favorite.internal.dto.FavoriteCreateDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceTest {

    @Mock private FavoriteRepository favoriteRepository;
    @Mock private FavoriteMapper favoriteMapper;
    @Mock private MangaService mangaService;

    @InjectMocks
    private FavoriteServiceImpl favoriteService;

    @Nested
    @DisplayName("add()")
    class Add {

        @Test
        @DisplayName("добавляет мангу в избранное")
        void shouldAddFavorite() {
            Long userId = 1L;
            FavoriteCreateDto dto = new FavoriteCreateDto();
            dto.setMangaId(10L);

            UserFavorite saved = UserFavorite.builder().id(1L).userId(userId).mangaId(10L).build();
            FavoriteDto favoriteDto = new FavoriteDto();
            MangaListDto mangaDto = new MangaListDto();
            mangaDto.setId(10L);

            when(favoriteRepository.existsByUserIdAndMangaId(userId, 10L)).thenReturn(false);
            when(favoriteRepository.save(any(UserFavorite.class))).thenReturn(saved);
            when(favoriteMapper.toDto(saved)).thenReturn(favoriteDto);
            when(mangaService.getMangaListById(10L)).thenReturn(mangaDto);

            FavoriteDto result = favoriteService.add(userId, dto);

            assertThat(result).isEqualTo(favoriteDto);
            assertThat(favoriteDto.getMangaListDto()).isEqualTo(mangaDto);

            ArgumentCaptor<UserFavorite> captor = ArgumentCaptor.forClass(UserFavorite.class);
            verify(favoriteRepository).save(captor.capture());

            UserFavorite captured = captor.getValue();
            assertThat(captured.getUserId()).isEqualTo(userId);
            assertThat(captured.getMangaId()).isEqualTo(10L);

            verify(favoriteMapper).toDto(saved);
            verify(mangaService).getMangaListById(10L);
        }

        @Test
        @DisplayName("выбрасывает ConflictException если уже в избранном")
        void shouldThrowConflictIfAlreadyFavorite() {
            Long userId = 1L;
            FavoriteCreateDto dto = new FavoriteCreateDto();
            dto.setMangaId(10L);

            when(favoriteRepository.existsByUserIdAndMangaId(userId, 10L)).thenReturn(true);

            assertThatThrownBy(() -> favoriteService.add(userId, dto))
                    .isInstanceOf(ConflictException.class);

            verify(favoriteRepository, never()).save(any());
            verifyNoInteractions(favoriteMapper);
            verifyNoInteractions(mangaService);
        }
    }

    @Nested
    @DisplayName("remove()")
    class Remove {

        @Test
        @DisplayName("удаляет мангу из избранного")
        void shouldRemoveFavorite() {
            Long userId = 1L;
            Long mangaId = 10L;

            when(favoriteRepository.existsByUserIdAndMangaId(userId, mangaId)).thenReturn(true);

            favoriteService.remove(userId, mangaId);

            verify(favoriteRepository).deleteByUserIdAndMangaId(userId, mangaId);
        }

        @Test
        @DisplayName("выбрасывает NotFoundException если не в избранном")
        void shouldThrowNotFoundIfNotFavorite() {
            Long userId = 1L;
            Long mangaId = 10L;

            when(favoriteRepository.existsByUserIdAndMangaId(userId, mangaId)).thenReturn(false);

            assertThatThrownBy(() -> favoriteService.remove(userId, mangaId))
                    .isInstanceOf(NotFoundException.class);

            verify(favoriteRepository, never()).deleteByUserIdAndMangaId(any(), any());
        }
    }

    @Nested
    @DisplayName("getUserFavorites()")
    class GetUserFavorites {

        @Test
        @DisplayName("возвращает избранное пользователя с данными манги")
        void shouldReturnFavoritesWithMangaData() {
            Long userId = 1L;
            Pageable pageable = PageRequest.of(0, 10);

            UserFavorite favorite = UserFavorite.builder().id(1L).userId(userId).mangaId(10L).build();
            Page<UserFavorite> favPage = new PageImpl<>(List.of(favorite));
            FavoriteDto favoriteDto = new FavoriteDto();
            MangaListDto mangaDto = new MangaListDto();
            mangaDto.setId(10L);

            when(favoriteRepository.findByUserIdOrderByAddedAtDesc(userId, pageable)).thenReturn(favPage);
            when(mangaService.getMangaListByIds(Set.of(10L))).thenReturn(List.of(mangaDto));
            when(favoriteMapper.toDto(favorite)).thenReturn(favoriteDto);

            Page<FavoriteDto> result = favoriteService.getUserFavorites(userId, pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(favoriteDto.getMangaListDto()).isEqualTo(mangaDto);
            verify(mangaService, times(1)).getMangaListByIds(any());
            verify(favoriteMapper).toDto(favorite);
        }

        @Test
        @DisplayName("возвращает пустую страницу если нет избранного")
        void shouldReturnEmptyPageIfNoFavorites() {
            Long userId = 1L;
            Pageable pageable = PageRequest.of(0, 10);

            when(favoriteRepository.findByUserIdOrderByAddedAtDesc(userId, pageable))
                    .thenReturn(Page.empty());
            when(mangaService.getMangaListByIds(Set.of())).thenReturn(List.of());

            Page<FavoriteDto> result = favoriteService.getUserFavorites(userId, pageable);

            assertThat(result.getContent()).isEmpty();

            verify(favoriteMapper, never()).toDto(any());
        }

        @Test
        @DisplayName("корректно обрабатывает несколько избранных")
        void shouldHandleMultipleFavorites() {
            Long userId = 1L;
            Pageable pageable = PageRequest.of(0, 10);

            UserFavorite fav1 = UserFavorite.builder().id(1L).userId(userId).mangaId(10L).build();
            UserFavorite fav2 = UserFavorite.builder().id(2L).userId(userId).mangaId(20L).build();
            FavoriteDto dto1 = new FavoriteDto();
            FavoriteDto dto2 = new FavoriteDto();
            MangaListDto manga1 = new MangaListDto(); manga1.setId(10L);
            MangaListDto manga2 = new MangaListDto(); manga2.setId(20L);

            when(favoriteRepository.findByUserIdOrderByAddedAtDesc(userId, pageable))
                    .thenReturn(new PageImpl<>(List.of(fav1, fav2)));
            when(mangaService.getMangaListByIds(Set.of(10L, 20L)))
                    .thenReturn(List.of(manga1, manga2));
            when(favoriteMapper.toDto(fav1)).thenReturn(dto1);
            when(favoriteMapper.toDto(fav2)).thenReturn(dto2);

            favoriteService.getUserFavorites(userId, pageable);

            assertThat(dto1.getMangaListDto()).isEqualTo(manga1);
            assertThat(dto2.getMangaListDto()).isEqualTo(manga2);
            verify(mangaService, times(1)).getMangaListByIds(any());
        }
    }

    @Nested
    @DisplayName("isFavorite()")
    class IsFavorite {

        @Test
        @DisplayName("возвращает true если манга в избранном")
        void shouldReturnTrueIfFavorite() {
            when(favoriteRepository.existsByUserIdAndMangaId(1L, 10L)).thenReturn(true);

            assertThat(favoriteService.isFavorite(1L, 10L)).isTrue();
        }

        @Test
        @DisplayName("возвращает false если манги нет в избранном")
        void shouldReturnFalseIfNotFavorite() {
            when(favoriteRepository.existsByUserIdAndMangaId(1L, 10L)).thenReturn(false);

            assertThat(favoriteService.isFavorite(1L, 10L)).isFalse();
        }
    }

    @Nested
    @DisplayName("countByManga()")
    class CountByManga {

        @Test
        @DisplayName("возвращает количество добавлений в избранное")
        void shouldReturnFavoriteCount() {
            when(favoriteRepository.countByMangaId(10L)).thenReturn(42L);

            assertThat(favoriteService.countByManga(10L)).isEqualTo(42L);
            verify(favoriteRepository).countByMangaId(10L);
        }

        @Test
        @DisplayName("возвращает 0 если никто не добавлял в избранное")
        void shouldReturnZeroIfNoFavorites() {
            when(favoriteRepository.countByMangaId(10L)).thenReturn(0L);

            assertThat(favoriteService.countByManga(10L)).isZero();
            verify(favoriteRepository).countByMangaId(10L);
        }
    }
}