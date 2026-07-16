package az.manga.demo.usercontent.service;

import az.manga.demo.common.exception.NotFoundException;
import az.manga.demo.usercontent.rating.api.RatingService;
import az.manga.demo.usercontent.rating.api.dto.RatingDto;
import az.manga.demo.usercontent.rating.internal.Rating;
import az.manga.demo.usercontent.rating.internal.RatingMapper;
import az.manga.demo.usercontent.rating.internal.RatingRepository;
import az.manga.demo.usercontent.rating.internal.dto.RatingCreateDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RatingServiceTest {
    @Mock
    private RatingRepository ratingRepository;

    @Mock
    private RatingMapper ratingMapper;

    @InjectMocks
    private RatingService ratingService;

    @Nested
    @DisplayName("rateOrUpdate()")
    class RateOrUpdate {

        @Test
        @DisplayName("создаёт новый рейтинг если ещё не оценивал")
        void shouldCreateNewRating() {
            Long userId = 1L;
            RatingCreateDto request = new RatingCreateDto(10L, 8);
            Rating newRating = Rating.builder().userId(userId).mangaId(10L).score(8).build();
            Rating savedRating = Rating.builder().id(1L).userId(userId).mangaId(10L).score(8).build();
            RatingDto expectedDto = new RatingDto(1L, 10L, userId, 8, null, null);

            when(ratingRepository.findByUserIdAndMangaId(userId, request.getMangaId()))
                    .thenReturn(Optional.empty());
            when(ratingMapper.toEntity(request)).thenReturn(newRating);
            when(ratingRepository.save(any(Rating.class))).thenReturn(savedRating);
            when(ratingMapper.toDto(savedRating)).thenReturn(expectedDto);

            RatingDto result = ratingService.rateOrUpdate(request, userId);

            assertThat(result).isEqualTo(expectedDto);

            ArgumentCaptor<Rating> captor = ArgumentCaptor.forClass(Rating.class);
            verify(ratingRepository).save(captor.capture());

            Rating saved = captor.getValue();
            assertThat(saved.getUserId()).isEqualTo(userId);
            assertThat(saved.getScore()).isEqualTo(8);
        }

        @Test
        @DisplayName("обновляет существующий рейтинг")
        void shouldUpdateExistingRating() {
            Long userId = 1L;
            RatingCreateDto request = new RatingCreateDto(10L, 9);
            Rating existing = Rating.builder().id(1L).userId(userId).mangaId(10L).score(5).build();
            Rating savedRating = Rating.builder().id(1L).userId(userId).mangaId(10L).score(9).build();
            RatingDto expectedDto = new RatingDto(1L, 10L, userId, 9, null, null);

            when(ratingRepository.findByUserIdAndMangaId(userId, request.getMangaId()))
                    .thenReturn(Optional.of(existing));
            when(ratingRepository.save(existing)).thenReturn(savedRating);
            when(ratingMapper.toDto(savedRating)).thenReturn(expectedDto);

            RatingDto result = ratingService.rateOrUpdate(request, userId);

            assertThat(result).isEqualTo(expectedDto);
            verify(ratingMapper, never()).toEntity(any());
            verify(ratingMapper).toDto(savedRating);
        }
    }

    @Nested
    @DisplayName("deleteRating()")
    class DeleteRating {

        @Test
        @DisplayName("удаляет рейтинг если существует")
        void shouldDeleteRating() {
            Long mangaId = 10L;
            Long userId = 1L;

            when(ratingRepository.existsByUserIdAndMangaId(userId, mangaId)).thenReturn(true);

            ratingService.deleteRating(mangaId, userId);

            verify(ratingRepository).deleteByUserIdAndMangaId(userId, mangaId);
        }

        @Test
        @DisplayName("выбрасывает NotFoundException если рейтинга нет")
        void shouldThrowNotFoundIfRatingDoesNotExist() {
            Long mangaId = 10L;
            Long userId = 1L;

            when(ratingRepository.existsByUserIdAndMangaId(userId, mangaId)).thenReturn(false);

            assertThatThrownBy(() -> ratingService.deleteRating(mangaId, userId))
                    .isInstanceOf(NotFoundException.class);

            verify(ratingRepository, never()).deleteByUserIdAndMangaId(any(), any());
        }
    }

    @Nested
    @DisplayName("getUserRating()")
    class GetUserRating {

        @Test
        @DisplayName("возвращает рейтинг пользователя")
        void shouldReturnUserRating() {
            Long mangaId = 10L;
            Long userId = 1L;
            Rating rating = Rating.builder().id(1L).userId(userId).mangaId(mangaId).score(7).build();
            RatingDto expectedDto = new RatingDto(1L, mangaId, userId, 7, null, null);

            when(ratingRepository.findByUserIdAndMangaId(userId, mangaId))
                    .thenReturn(Optional.of(rating));
            when(ratingMapper.toDto(rating)).thenReturn(expectedDto);

            RatingDto result = ratingService.getUserRating(mangaId, userId);

            assertThat(result).isEqualTo(expectedDto);

            verify(ratingRepository).findByUserIdAndMangaId(userId, mangaId);
            verify(ratingMapper).toDto(rating);
        }

        @Test
        @DisplayName("выбрасывает NotFoundException если рейтинга нет")
        void shouldThrowNotFoundIfRatingDoesNotExist() {
            Long mangaId = 10L;
            Long userId = 1L;

            when(ratingRepository.findByUserIdAndMangaId(userId, mangaId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> ratingService.getUserRating(mangaId, userId))
                    .isInstanceOf(NotFoundException.class);

            verifyNoInteractions(ratingMapper);
        }
    }

    @Nested
    @DisplayName("getAverageRating()")
    class GetAverageRating {

        @Test
        @DisplayName("возвращает средний рейтинг")
        void shouldReturnAverageRating() {
            Long mangaId = 10L;
            when(ratingRepository.calculateAverageRating(mangaId)).thenReturn(7.5);

            double result = ratingService.getAverageRating(mangaId);

            assertThat(result).isEqualTo(7.5);

            verify(ratingRepository).calculateAverageRating(mangaId);
        }

        @Test
        @DisplayName("возвращает 0.0 если нет оценок")
        void shouldReturnZeroIfNoRatings() {
            Long mangaId = 10L;
            when(ratingRepository.calculateAverageRating(mangaId)).thenReturn(0.0);

            double result = ratingService.getAverageRating(mangaId);

            assertThat(result).isZero();

            verify(ratingRepository).calculateAverageRating(mangaId);
        }
    }

    @Nested
    @DisplayName("getVoteCount()")
    class GetVoteCount {

        @Test
        @DisplayName("возвращает количество голосов")
        void shouldReturnVoteCount() {
            Long mangaId = 10L;
            when(ratingRepository.countByMangaId(mangaId)).thenReturn(42L);

            Long result = ratingService.getVoteCount(mangaId);

            assertThat(result).isEqualTo(42L);

            verify(ratingRepository).countByMangaId(mangaId);
        }

        @Test
        @DisplayName("возвращает 0 если голосов нет")
        void shouldReturnZeroIfNoVotes() {
            Long mangaId = 10L;
            when(ratingRepository.countByMangaId(mangaId)).thenReturn(0L);

            Long result = ratingService.getVoteCount(mangaId);

            assertThat(result).isZero();

            verify(ratingRepository).countByMangaId(mangaId);
        }
    }
}
