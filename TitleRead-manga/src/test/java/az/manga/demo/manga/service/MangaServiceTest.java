package az.manga.demo.manga.service;


import az.manga.demo.common.event.MangaViewedEvent;
import az.manga.demo.common.exception.NotFoundException;
import az.manga.demo.manga.catalog.internal.Genre;
import az.manga.demo.manga.catalog.internal.GenreRepository;
import az.manga.demo.manga.catalog.internal.Tag;
import az.manga.demo.manga.catalog.internal.TagRepository;
import az.manga.demo.manga.manga.api.dto.MangaDto;
import az.manga.demo.manga.manga.api.dto.MangaListDto;
import az.manga.demo.manga.manga.internal.Manga;
import az.manga.demo.manga.manga.internal.MangaMapper;
import az.manga.demo.manga.manga.internal.MangaRepository;
import az.manga.demo.manga.manga.internal.MangaServiceImpl;
import az.manga.demo.manga.manga.internal.dto.MangaCreateDto;
import az.manga.demo.manga.manga.internal.dto.MangaUpdateDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(MockitoExtension.class)
class MangaServiceTest {

    @Mock private MangaRepository mangaRepository;
    @Mock private MangaMapper mangaMapper;
    @Mock private GenreRepository genreRepository;
    @Mock private TagRepository tagRepository;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private MangaServiceImpl mangaService;

    private Manga buildManga(Long id) {
        Manga manga = new Manga();
        manga.setId(id);
        manga.setTitle("OnePiece");
        return manga;
    }

    private MangaDto buildMangaDto(Long id) {
        MangaDto dto = new MangaDto();
        dto.setId(id);
        dto.setTitle("OnePiece");
        return dto;
    }

    private MangaListDto buildMangaListDto(Long id) {
        MangaListDto dto = new MangaListDto();
        dto.setId(id);
        dto.setTitle("Test Manga");
        return dto;
    }

    private Genre buildGenre() {
        Genre genre = new Genre();
        genre.setName("Shounen");
        return genre;
    }

    private Tag buildTag() {
        Tag tag = new Tag();
        tag.setName("#MC male");
        return tag;
    }

    @Nested
    @DisplayName("getById()")
    class GetById {

        @Test
        @DisplayName("возвращает MangaDto если манга найдена")
        void shouldReturnMangaDto() {
            Manga manga = buildManga(2L);
            MangaDto expectedDto = buildMangaDto(2L);

            Mockito.when(mangaRepository.findById(2L)).thenReturn(Optional.of(manga));
            Mockito.when(mangaMapper.toDto(manga)).thenReturn(expectedDto);

            MangaDto result = mangaService.getById(2L);

            assertThat(result).isEqualTo(expectedDto);
            Mockito.verify(mangaRepository).findById(2L);
            Mockito.verify(mangaMapper).toDto(manga);
        }

        @Test
        @DisplayName("выбрасывает NotFoundException если манга не найдена")
        void shouldThrowNotFoundIfMangaNotFound() {
            Mockito.when(mangaRepository.findById(99L)).thenReturn(Optional.empty());

            Assertions.assertThatThrownBy(() -> mangaService.getById(99L))
                    .isInstanceOf(NotFoundException.class);

            Mockito.verifyNoInteractions(mangaMapper);
        }
    }

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("создаёт мангу с жанрами и тегами")
        void shouldCreateMangaWithGenresAndTags() {
            MangaCreateDto dto = new MangaCreateDto();
            dto.setTitle("Naruto");
            dto.setGenreIds(Set.of(1L));
            dto.setTagIds(Set.of(2L));

            Manga manga = buildManga(1L);
            MangaDto expectedDto = buildMangaDto(1L);
            Genre genre = buildGenre();
            Tag tag = buildTag();

            Mockito.when(mangaMapper.toEntity(dto)).thenReturn(manga);
            Mockito.when(genreRepository.findAllById(Set.of(1L))).thenReturn(List.of(genre));
            Mockito.when(tagRepository.findAllById(Set.of(2L))).thenReturn(List.of(tag));
            Mockito.when(mangaRepository.save(manga)).thenReturn(manga);
            Mockito.when(mangaMapper.toDto(manga)).thenReturn(expectedDto);

            MangaDto result = mangaService.create(dto);

            assertThat(result).isEqualTo(expectedDto);
            ArgumentCaptor<Manga> captor = ArgumentCaptor.forClass(Manga.class);
            Mockito.verify(mangaRepository).save(captor.capture());

            Manga savedManga = captor.getValue();
            assertThat(savedManga.getGenres()).containsExactly(genre);
            assertThat(savedManga.getTags()).containsExactly(tag);

        }

        @Test
        @DisplayName("создаёт мангу без жанров если они null")
        void shouldCreateMangaWithoutGenres() {
            MangaCreateDto dto = new MangaCreateDto();
            dto.setTitle("Naruto");
            dto.setGenreIds(null);
            dto.setTagIds(Set.of(2L));

            Manga manga = buildManga(1L);
            MangaDto expectedDto = buildMangaDto(1L);
            Tag tag = buildTag();

            Mockito.when(mangaMapper.toEntity(dto)).thenReturn(manga);
            Mockito.when(tagRepository.findAllById(Set.of(2L))).thenReturn(List.of(tag));
            Mockito.when(mangaRepository.save(manga)).thenReturn(manga);
            Mockito.when(mangaMapper.toDto(manga)).thenReturn(expectedDto);

            MangaDto result = mangaService.create(dto);

            assertThat(result).isEqualTo(expectedDto);
            ArgumentCaptor<Manga> captor = ArgumentCaptor.forClass(Manga.class);
            Mockito.verify(mangaRepository).save(captor.capture());

            Manga savedManga = captor.getValue();
            assertThat(savedManga.getTags()).containsExactly(tag);
            Mockito.verify(genreRepository, Mockito.never()).findAllById(ArgumentMatchers.any());
        }

        @Test
        @DisplayName("создаёт мангу без тегов если они null")
        void shouldCreateMangaWithoutTags() {
            MangaCreateDto dto = new MangaCreateDto();
            dto.setTitle("Naruto");
            dto.setGenreIds(Set.of(1L));
            dto.setTagIds(null);

            Manga manga = buildManga(1L);
            MangaDto expectedDto = buildMangaDto(1L);
            Genre genre = buildGenre();

            Mockito.when(mangaMapper.toEntity(dto)).thenReturn(manga);
            Mockito.when(genreRepository.findAllById(Set.of(1L))).thenReturn(List.of(genre));
            Mockito.when(mangaRepository.save(manga)).thenReturn(manga);
            Mockito.when(mangaMapper.toDto(manga)).thenReturn(expectedDto);

            MangaDto result = mangaService.create(dto);

            assertThat(result).isEqualTo(expectedDto);
            ArgumentCaptor<Manga> captor = ArgumentCaptor.forClass(Manga.class);
            Mockito.verify(mangaRepository).save(captor.capture());

            Manga savedManga = captor.getValue();
            assertThat(savedManga.getGenres()).containsExactly(genre);
            Mockito.verify(tagRepository, Mockito.never()).findAllById(ArgumentMatchers.any());
        }

        @Test
        @DisplayName("создаёт мангу без жанров и тегов если они null")
        void shouldCreateMangaWithoutGenresAndTags() {
            MangaCreateDto dto = new MangaCreateDto();
            dto.setTitle("Naruto");
            dto.setGenreIds(null);
            dto.setTagIds(null);

            Manga manga = buildManga(1L);
            MangaDto expectedDto = buildMangaDto(1L);

            Mockito.when(mangaMapper.toEntity(dto)).thenReturn(manga);
            Mockito.when(mangaRepository.save(manga)).thenReturn(manga);
            Mockito.when(mangaMapper.toDto(manga)).thenReturn(expectedDto);

            mangaService.create(dto);

            Mockito.verify(genreRepository, Mockito.never()).findAllById(ArgumentMatchers.any());
            Mockito.verify(tagRepository, Mockito.never()).findAllById(ArgumentMatchers.any());
        }
    }

    @Nested
    @DisplayName("update()")
    class Update {

        @Test
        @DisplayName("обновляет мангу")
        void shouldUpdateMangaWithoutGenresAndTags() {
            MangaUpdateDto dto = new MangaUpdateDto();
            dto.setTitle("One Piece Updated");
            dto.setGenreIds(Set.of());
            dto.setTagIds(Set.of());

            Manga manga = buildManga(1L);
            MangaDto expectedDto = buildMangaDto(1L);
            expectedDto.setTitle("One Piece Updated");

            Mockito.when(mangaRepository.findById(1L)).thenReturn(Optional.of(manga));
            Mockito.doNothing().when(mangaMapper).updateEntity(manga, dto);
            Mockito.when(mangaRepository.save(manga)).thenReturn(manga);
            Mockito.when(mangaMapper.toDto(manga)).thenReturn(expectedDto);

            MangaDto result = mangaService.update(1L, dto);

            assertThat(result).isEqualTo(expectedDto);
            Mockito.verify(mangaMapper).updateEntity(manga, dto);
            Mockito.verify(genreRepository, Mockito.never()).findAllById(ArgumentMatchers.any());
            Mockito.verify(tagRepository, Mockito.never()).findAllById(ArgumentMatchers.any());
        }

        @Test
        @DisplayName("обновляет мангу с жанрами и тегами")
        void shouldUpdateMangaWithGenresAndTags() {
            MangaUpdateDto dto = new MangaUpdateDto();
            dto.setGenreIds(Set.of(1L));
            dto.setTagIds(Set.of(2L));

            Manga manga = buildManga(1L);
            MangaDto expectedDto = buildMangaDto(1L);
            Genre genre = buildGenre();
            Tag tag = buildTag();

            Mockito.when(mangaRepository.findById(1L)).thenReturn(Optional.of(manga));
            Mockito.doNothing().when(mangaMapper).updateEntity(manga, dto);
            Mockito.when(genreRepository.findAllById(Set.of(1L))).thenReturn(List.of(genre));
            Mockito.when(tagRepository.findAllById(Set.of(2L))).thenReturn(List.of(tag));
            Mockito.when(mangaRepository.save(manga)).thenReturn(manga);
            Mockito.when(mangaMapper.toDto(manga)).thenReturn(expectedDto);

            mangaService.update(1L, dto);

            ArgumentCaptor<Manga> captor = ArgumentCaptor.forClass(Manga.class);
            Mockito.verify(mangaRepository).save(captor.capture());

            Manga savedManga = captor.getValue();
            assertThat(savedManga.getGenres()).containsExactly(genre);
            assertThat(savedManga.getTags()).containsExactly(tag);
        }

        @Test
        @DisplayName("обновляет мангу с жанрами без тегов")
        void shouldUpdateMangaWithGenres() {
            MangaUpdateDto dto = new MangaUpdateDto();
            dto.setGenreIds(Set.of(1L));
            dto.setTagIds(Set.of());

            Manga manga = buildManga(1L);
            MangaDto expectedDto = buildMangaDto(1L);
            Genre genre = buildGenre();


            Mockito.when(mangaRepository.findById(1L)).thenReturn(Optional.of(manga));
            Mockito.doNothing().when(mangaMapper).updateEntity(manga, dto);
            Mockito.when(genreRepository.findAllById(Set.of(1L))).thenReturn(List.of(genre));
            Mockito.when(mangaRepository.save(manga)).thenReturn(manga);
            Mockito.when(mangaMapper.toDto(manga)).thenReturn(expectedDto);

            mangaService.update(1L, dto);

            ArgumentCaptor<Manga> captor = ArgumentCaptor.forClass(Manga.class);
            Mockito.verify(mangaRepository).save(captor.capture());

            Manga savedManga = captor.getValue();
            assertThat(savedManga.getGenres()).containsExactly(genre);

            Mockito.verify(tagRepository, Mockito.never()).findAllById(ArgumentMatchers.any());
        }

        @Test
        @DisplayName("обновляет мангу с тегами без жанров")
        void shouldUpdateMangaWithTags() {
            MangaUpdateDto dto = new MangaUpdateDto();
            dto.setGenreIds(Set.of());
            dto.setTagIds(Set.of(2L));

            Manga manga = buildManga(1L);
            MangaDto expectedDto = buildMangaDto(1L);
            Tag tag = buildTag();

            Mockito.when(mangaRepository.findById(1L)).thenReturn(Optional.of(manga));
            Mockito.doNothing().when(mangaMapper).updateEntity(manga, dto);

            Mockito.when(tagRepository.findAllById(Set.of(2L))).thenReturn(List.of(tag));
            Mockito.when(mangaRepository.save(manga)).thenReturn(manga);
            Mockito.when(mangaMapper.toDto(manga)).thenReturn(expectedDto);
            mangaService.update(1L, dto);

            ArgumentCaptor<Manga> captor = ArgumentCaptor.forClass(Manga.class);
            Mockito.verify(mangaRepository).save(captor.capture());

            Manga savedManga = captor.getValue();
            assertThat(savedManga.getTags()).containsExactly(tag);

            Mockito.verify(genreRepository, Mockito.never()).findAllById(ArgumentMatchers.any());
        }

        @Test
        @DisplayName("выбрасывает NotFoundException если манга не найдена")
        void shouldThrowNotFoundIfMangaNotFound() {
            Mockito.when(mangaRepository.findById(99L)).thenReturn(Optional.empty());

            Assertions.assertThatThrownBy(() -> mangaService.update(99L, new MangaUpdateDto()))
                    .isInstanceOf(NotFoundException.class);

            Mockito.verify(mangaRepository, Mockito.never()).save(ArgumentMatchers.any());
            Mockito.verifyNoInteractions(mangaMapper);
        }
    }

    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("удаляет мангу если существует")
        void shouldDeleteManga() {
            Mockito.when(mangaRepository.existsById(1L)).thenReturn(true);

            mangaService.delete(1L);

            Mockito.verify(mangaRepository).deleteById(1L);
        }

        @Test
        @DisplayName("выбрасывает NotFoundException если манга не найдена")
        void shouldThrowNotFoundIfMangaNotFound() {
            Mockito.when(mangaRepository.existsById(99L)).thenReturn(false);

            Assertions.assertThatThrownBy(() -> mangaService.delete(99L))
                    .isInstanceOf(NotFoundException.class);

            Mockito.verify(mangaRepository, Mockito.never()).deleteById(ArgumentMatchers.any());
        }
    }

    @Nested
    @DisplayName("getAll()")
    class GetAll {

        @Test
        @DisplayName("возвращает страницу MangaListDto")
        void shouldReturnPageOfMangaListDto() {
            Pageable pageable = PageRequest.of(0, 20);
            Manga manga = buildManga(1L);
            MangaListDto listDto = buildMangaListDto(1L);

            Mockito.when(mangaRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(manga)));
            Mockito.when(mangaMapper.toListDto(manga)).thenReturn(listDto);

            Page<MangaListDto> result = mangaService.getAll(pageable);

            Assertions.assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().getFirst().getId()).isEqualTo(1L);
            Mockito.verify(mangaMapper).toListDto(manga);
        }

        @Test
        @DisplayName("возвращает пустую страницу если манги нет")
        void shouldReturnEmptyPage() {
            Pageable pageable = PageRequest.of(0, 20);
            Mockito.when(mangaRepository.findAll(pageable)).thenReturn(Page.empty());

            Page<MangaListDto> result = mangaService.getAll(pageable);

            Assertions.assertThat(result.getContent()).isEmpty();
            Mockito.verifyNoInteractions(mangaMapper);
        }
    }

    @Nested
    @DisplayName("registerView()")
    class RegisterView {

        @Test
        @DisplayName("публикует MangaViewedEvent")
        void shouldPublishMangaViewedEvent() {
            mangaService.registerView(1L, 5L);

            ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
            Mockito.verify(eventPublisher).publishEvent(captor.capture());

            MangaViewedEvent event = (MangaViewedEvent) captor.getValue();
            assertThat(event.mangaId()).isEqualTo(1L);
            assertThat(event.userId()).isEqualTo(5L);
            Mockito.verify(eventPublisher).publishEvent(ArgumentMatchers.any(MangaViewedEvent.class));
        }

        @Test
        @DisplayName("публикует событие даже если userId == null")
        void shouldPublishEventWhenUserIsNull() {
            mangaService.registerView(1L, null);

            Mockito.verify(eventPublisher).publishEvent(ArgumentMatchers.any(MangaViewedEvent.class));
        }
    }
}