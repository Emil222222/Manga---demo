package az.manga.demo.manga.service;

import az.manga.demo.common.exception.ConflictException;
import az.manga.demo.common.exception.NotFoundException;
import az.manga.demo.manga.catalog.api.dto.GenreDto;
import az.manga.demo.manga.catalog.internal.Genre;
import az.manga.demo.manga.catalog.internal.GenreMapper;
import az.manga.demo.manga.catalog.internal.GenreRepository;
import az.manga.demo.manga.catalog.internal.GenreServiceImpl;
import az.manga.demo.manga.catalog.internal.dto.GenreCreateDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class GenreServiceTest {

    @Mock private GenreRepository genreRepository;
    @Mock private GenreMapper genreMapper;

    @InjectMocks
    private GenreServiceImpl genreService;

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("создаёт жанр если slug уникален")
        void shouldCreateGenre() {
            GenreCreateDto dto = new GenreCreateDto("Action", "action", null);
            Genre entity = new Genre();
            Genre saved = new Genre();
            GenreDto expectedDto = new GenreDto(1L, "Action", "action", null);

            Mockito.when(genreRepository.existsBySlug(dto.getSlug())).thenReturn(false);
            Mockito.when(genreMapper.toEntity(dto)).thenReturn(entity);
            Mockito.when(genreRepository.save(entity)).thenReturn(saved);
            Mockito.when(genreMapper.toDto(saved)).thenReturn(expectedDto);

            GenreDto result = genreService.create(dto);

            assertThat(result).isEqualTo(expectedDto);

            Mockito.verify(genreRepository).save(entity);
        }

        @Test
        @DisplayName("выбрасывает ConflictException если slug уже существует")
        void shouldThrowConflictIfSlugExists() {
            GenreCreateDto dto = new GenreCreateDto("Action", "action", null);
            Mockito.when(genreRepository.existsBySlug(dto.getSlug())).thenReturn(true);

            Assertions.assertThatThrownBy(() -> genreService.create(dto))
                    .isInstanceOf(ConflictException.class);

            Mockito.verify(genreRepository, Mockito.never()).save(ArgumentMatchers.any());
            Mockito.verifyNoInteractions(genreMapper);
        }
    }

    @Nested
    @DisplayName("getById()")
    class GetById {

        @Test
        @DisplayName("возвращает жанр по id")
        void shouldReturnGenreById() {
            Genre genre = new Genre();
            GenreDto expectedDto = new GenreDto(1L, "Action", "action", null);

            Mockito.when(genreRepository.findById(1L)).thenReturn(Optional.of(genre));
            Mockito.when(genreMapper.toDto(genre)).thenReturn(expectedDto);

            GenreDto result = genreService.getById(1L);

            assertThat(result).isEqualTo(expectedDto);

        }

        @Test
        @DisplayName("выбрасывает NotFoundException если жанр не найден")
        void shouldThrowNotFoundIfGenreNotFound() {
            Mockito.when(genreRepository.findById(99L)).thenReturn(Optional.empty());

            Assertions.assertThatThrownBy(() -> genreService.getById(99L))
                    .isInstanceOf(NotFoundException.class);

            Mockito.verifyNoInteractions(genreMapper);
        }
    }

    @Nested
    @DisplayName("getAll()")
    class GetAll {

        @Test
        @DisplayName("возвращает список всех жанров")
        void shouldReturnAllGenres() {
            Genre g1 = new Genre();
            Genre g2 = new Genre();
            GenreDto dto1 = new GenreDto(1L, "Action", "action", null);
            GenreDto dto2 = new GenreDto(2L, "Comedy", "comedy", null);

            Mockito.when(genreRepository.findAllByOrderByNameAsc()).thenReturn(List.of(g1, g2));
            Mockito.when(genreMapper.toDto(g1)).thenReturn(dto1);
            Mockito.when(genreMapper.toDto(g2)).thenReturn(dto2);

            List<GenreDto> result = genreService.getAll();

            Assertions.assertThat(result).containsExactly(dto1, dto2);
        }

        @Test
        @DisplayName("возвращает пустой список если жанров нет")
        void shouldReturnEmptyListIfNoGenres() {
            Mockito.when(genreRepository.findAllByOrderByNameAsc()).thenReturn(List.of());

            List<GenreDto> result = genreService.getAll();

            Assertions.assertThat(result).isEmpty();
            Mockito.verifyNoInteractions(genreMapper);
        }
    }

    @Nested
    @DisplayName("update()")
    class Update {

        @Test
        @DisplayName("обновляет жанр")
        void shouldUpdateGenre() {
            Long id = 1L;
            GenreCreateDto dto = new GenreCreateDto("Drama", "drama", null);
            Genre genre = new Genre();
            Genre saved = new Genre();
            GenreDto expectedDto = new GenreDto(id, "Drama", "drama", null);

            Mockito.when(genreRepository.findById(id)).thenReturn(Optional.of(genre));
            Mockito.doNothing().when(genreMapper).updateEntity(genre, dto);
            Mockito.when(genreRepository.save(genre)).thenReturn(saved);
            Mockito.when(genreMapper.toDto(saved)).thenReturn(expectedDto);

            GenreDto result = genreService.update(id, dto);

            assertThat(result).isEqualTo(expectedDto);

            Mockito.verify(genreMapper).updateEntity(genre, dto);
            Mockito.verify(genreRepository).save(genre);
        }

        @Test
        @DisplayName("выбрасывает NotFoundException если жанр не найден")
        void shouldThrowNotFoundIfGenreNotFound() {
            Mockito.when(genreRepository.findById(99L)).thenReturn(Optional.empty());

            Assertions.assertThatThrownBy(() -> genreService.update(99L, new GenreCreateDto()))
                    .isInstanceOf(NotFoundException.class);

            Mockito.verify(genreRepository, Mockito.never()).save(ArgumentMatchers.any());
            Mockito.verifyNoInteractions(genreMapper);
        }
    }

    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("удаляет жанр если существует")
        void shouldDeleteGenre() {
            Mockito.when(genreRepository.existsById(1L)).thenReturn(true);

            genreService.delete(1L);

            Mockito.verify(genreRepository).deleteById(1L);
        }

        @Test
        @DisplayName("выбрасывает NotFoundException если жанр не найден")
        void shouldThrowNotFoundIfGenreNotFound() {
            Mockito.when(genreRepository.existsById(99L)).thenReturn(false);

            Assertions.assertThatThrownBy(() -> genreService.delete(99L))
                    .isInstanceOf(NotFoundException.class);

            Mockito.verify(genreRepository, Mockito.never()).deleteById(ArgumentMatchers.any());
        }
    }
}