package az.manga.demo.manga.service;

import az.manga.demo.common.event.ChapterReadEvent;
import az.manga.demo.common.exception.ConflictException;
import az.manga.demo.common.exception.NotFoundException;
import az.manga.demo.manga.chapter.api.dto.ChapterDto;
import az.manga.demo.manga.chapter.api.dto.ChapterListDto;
import az.manga.demo.manga.chapter.internal.Chapter;
import az.manga.demo.manga.chapter.internal.ChapterMapper;
import az.manga.demo.manga.chapter.internal.ChapterRepository;
import az.manga.demo.manga.chapter.internal.ChapterServiceImpl;
import az.manga.demo.manga.chapter.internal.dto.ChapterCreateDto;
import az.manga.demo.manga.page.api.PageService;
import az.manga.demo.manga.page.api.dto.PageDto;
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
class ChapterServiceTest {

    @Mock private ChapterRepository chapterRepository;
    @Mock private ChapterMapper chapterMapper;
    @Mock private PageService pageService;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ChapterServiceImpl chapterService;

    @Nested
    @DisplayName("getChapter()")
    class GetChapter {

        @Test
        @DisplayName("возвращает главу с страницами и публикует событие если userId != null")
        void shouldReturnChapterAndPublishEvent() {
            Long chapterId = 1L;
            Long userId = 5L;
            Chapter chapter = Chapter.builder().id(chapterId).mangaId(10L).chapterNumber(1d).build();
            ChapterDto chapterDto = new ChapterDto();
            chapterDto.setId(chapterId);
            chapterDto.setMangaId(10L);
            chapterDto.setChapterNumber(1d);

            Mockito.when(chapterRepository.findById(chapterId)).thenReturn(Optional.of(chapter));
            Mockito.when(chapterMapper.toDto(chapter)).thenReturn(chapterDto);
            Mockito.when(pageService.getPagesByChapter(chapterId)).thenReturn(List.of());
            Mockito.when(chapterRepository.findByMangaIdAndChapterNumber(10L, 0d)).thenReturn(Optional.empty());
            Mockito.when(chapterRepository.findByMangaIdAndChapterNumber(10L, 2d)).thenReturn(Optional.empty());

            ChapterDto result = chapterService.getChapter(chapterId, userId);

            assertThat(result).isEqualTo(chapterDto);

            ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
            Mockito.verify(eventPublisher).publishEvent(captor.capture());

            ChapterReadEvent event = (ChapterReadEvent) captor.getValue();
            assertThat(event.mangaId()).isEqualTo(10L);
            assertThat(event.chapterId()).isEqualTo(chapterId);
            assertThat(event.userId()).isEqualTo(userId);
            assertThat(result.getPreviousChapterNumber()).isNull();
            assertThat(result.getNextChapterNumber()).isNull();

            Mockito.verify(chapterMapper).toDto(chapter);
            Mockito.verify(pageService).getPagesByChapter(chapterId);
        }

        @Test
        @DisplayName("не публикует событие если userId == null (анонимный)")
        void shouldNotPublishEventIfUserIsNull() {
            Long chapterId = 1L;
            Chapter chapter = Chapter.builder().id(chapterId).mangaId(10L).chapterNumber(1d).build();
            ChapterDto chapterDto = new ChapterDto();
            chapterDto.setId(chapterId);
            chapterDto.setMangaId(10L);
            chapterDto.setChapterNumber(1d);

            Mockito.when(chapterRepository.findById(chapterId)).thenReturn(Optional.of(chapter));
            Mockito.when(chapterMapper.toDto(chapter)).thenReturn(chapterDto);
            Mockito.when(pageService.getPagesByChapter(chapterId)).thenReturn(List.of());
            Mockito.when(chapterRepository.findByMangaIdAndChapterNumber(10L, 0d)).thenReturn(Optional.empty());
            Mockito.when(chapterRepository.findByMangaIdAndChapterNumber(10L, 2d)).thenReturn(Optional.empty());

            chapterService.getChapter(chapterId, null);

            Mockito.verify(eventPublisher, Mockito.never()).publishEvent(ArgumentMatchers.any());
            Mockito.verify(chapterMapper).toDto(chapter);
            Mockito.verify(pageService).getPagesByChapter(chapterId);
        }

        @Test
        @DisplayName("заполняет previousChapterNumber и nextChapterNumber если есть соседние главы")
        void shouldSetPreviousAndNextChapterNumbers() {
            Long chapterId = 2L;
            Chapter chapter = Chapter.builder().id(chapterId).mangaId(10L).chapterNumber(2d).build();
            Chapter prev = Chapter.builder().id(1L).mangaId(10L).chapterNumber(1d).build();
            Chapter next = Chapter.builder().id(3L).mangaId(10L).chapterNumber(3d).build();
            ChapterDto chapterDto = new ChapterDto();
            chapterDto.setId(chapterId);
            chapterDto.setMangaId(10L);
            chapterDto.setChapterNumber(2d);

            Mockito.when(chapterRepository.findById(chapterId)).thenReturn(Optional.of(chapter));
            Mockito.when(chapterMapper.toDto(chapter)).thenReturn(chapterDto);
            Mockito.when(pageService.getPagesByChapter(chapterId)).thenReturn(List.of());
            Mockito.when(chapterRepository.findByMangaIdAndChapterNumber(10L, 1d)).thenReturn(Optional.of(prev));
            Mockito.when(chapterRepository.findByMangaIdAndChapterNumber(10L, 3d)).thenReturn(Optional.of(next));

            ChapterDto result = chapterService.getChapter(chapterId, null);

            assertThat(result.getPreviousChapterNumber()).isEqualTo(1);
            assertThat(result.getNextChapterNumber()).isEqualTo(3);
            assertThat(result).isEqualTo(chapterDto);

            Mockito.verify(chapterMapper).toDto(chapter);
            Mockito.verify(pageService).getPagesByChapter(chapterId);
        }

        @Test
        @DisplayName("выбрасывает NotFoundException если глава не найдена")
        void shouldThrowNotFoundIfChapterNotFound() {
            Mockito.when(chapterRepository.findById(99L)).thenReturn(Optional.empty());

            Assertions.assertThatThrownBy(() -> chapterService.getChapter(99L, 1L))
                    .isInstanceOf(NotFoundException.class);

            Mockito.verifyNoInteractions(eventPublisher);
            Mockito.verifyNoInteractions(pageService);
        }
    }

    @Nested
    @DisplayName("createChapter()")
    class CreateChapter {

        @Test
        @DisplayName("создаёт главу без страниц")
        void shouldCreateChapterWithoutPages() {
            ChapterCreateDto dto = new ChapterCreateDto();
            dto.setMangaId(10L);
            dto.setChapterNumber(1d);
            dto.setPageImageUrls(null);

            Chapter chapter = Chapter.builder().mangaId(10L).chapterNumber(1d).build();
            Chapter saved = Chapter.builder().id(1L).mangaId(10L).chapterNumber(1d).build();
            ChapterDto chapterDto = new ChapterDto();
            chapterDto.setId(1L);
            chapterDto.setMangaId(10L);
            chapterDto.setChapterNumber(1d);

            Mockito.when(chapterRepository.findByMangaIdAndChapterNumber(10L, 1d)).thenReturn(Optional.empty());
            Mockito.when(chapterMapper.toEntity(dto)).thenReturn(chapter);
            Mockito.when(chapterRepository.save(chapter)).thenReturn(saved);
            Mockito.when(chapterMapper.toDto(saved)).thenReturn(chapterDto);

            ChapterDto result = chapterService.createChapter(dto);

            assertThat(result).isEqualTo(chapterDto);

            Mockito.verify(pageService, Mockito.never()).addPages(ArgumentMatchers.any(), ArgumentMatchers.any());
            Mockito.verify(chapterMapper).toEntity(dto);
            Mockito.verify(chapterRepository).save(chapter);
            Mockito.verify(chapterMapper).toDto(saved);
        }

        @Test
        @DisplayName("создаёт главу со страницами и вызывает addPages")
        void shouldCreateChapterWithPages() {
            List<String> pageUrls = List.of("url1", "url2", "url3");
            ChapterCreateDto dto = new ChapterCreateDto();
            dto.setMangaId(10L);
            dto.setChapterNumber(1d);
            dto.setPageImageUrls(pageUrls);

            Chapter chapter = Chapter.builder().mangaId(10L).chapterNumber(1d).build();
            Chapter saved = Chapter.builder().id(1L).mangaId(10L).chapterNumber(1d).build();
            ChapterDto chapterDto = new ChapterDto();
            chapterDto.setId(1L);
            chapterDto.setMangaId(10L);
            chapterDto.setChapterNumber(1d);

            Mockito.when(chapterRepository.findByMangaIdAndChapterNumber(10L, 1d)).thenReturn(Optional.empty());
            Mockito.when(chapterMapper.toEntity(dto)).thenReturn(chapter);
            Mockito.when(chapterRepository.save(chapter)).thenReturn(saved);
            Mockito.when(chapterMapper.toDto(saved)).thenReturn(chapterDto);
            Mockito.when(pageService.getPagesByChapter(1L)).thenReturn(List.of(new PageDto(), new PageDto(), new PageDto()));
            Mockito.when(chapterRepository.findByMangaIdAndChapterNumber(10L, 0d)).thenReturn(Optional.empty());
            Mockito.when(chapterRepository.findByMangaIdAndChapterNumber(10L, 2d)).thenReturn(Optional.empty());

            ChapterDto result = chapterService.createChapter(dto);

            assertThat(result.getPageCount()).isEqualTo(3);

            InOrder inOrder = Mockito.inOrder(chapterRepository, pageService);
            inOrder.verify(chapterRepository).save(chapter);
            inOrder.verify(pageService).addPages(1L, pageUrls);

            Mockito.verify(chapterMapper).toEntity(dto);
        }

        @Test
        @DisplayName("создаёт главу с пустым списком страниц — обрабатывает как без страниц")
        void shouldCreateChapterWithEmptyPageUrls() {
            ChapterCreateDto dto = new ChapterCreateDto();
            dto.setMangaId(10L);
            dto.setChapterNumber(1d);
            dto.setPageImageUrls(List.of());

            Chapter chapter = Chapter.builder().mangaId(10L).chapterNumber(1d).build();
            Chapter saved = Chapter.builder().id(1L).mangaId(10L).chapterNumber(1d).build();
            ChapterDto chapterDto = new ChapterDto();
            chapterDto.setId(1L);
            chapterDto.setMangaId(10L);
            chapterDto.setChapterNumber(1d);

            Mockito.when(chapterRepository.findByMangaIdAndChapterNumber(10L, 1d)).thenReturn(Optional.empty());
            Mockito.when(chapterMapper.toEntity(dto)).thenReturn(chapter);
            Mockito.when(chapterRepository.save(chapter)).thenReturn(saved);
            Mockito.when(chapterMapper.toDto(saved)).thenReturn(chapterDto);

            ChapterDto result = chapterService.createChapter(dto);

            assertThat(result).isEqualTo(chapterDto);
            Mockito.verify(pageService, Mockito.never()).addPages(ArgumentMatchers.any(), ArgumentMatchers.any());
            Mockito.verify(chapterRepository).save(chapter);
        }

        @Test
        @DisplayName("выбрасывает ConflictException если глава уже существует")
        void shouldThrowConflictIfChapterExists() {
            ChapterCreateDto dto = new ChapterCreateDto();
            dto.setMangaId(10L);
            dto.setChapterNumber(1d);

            Mockito.when(chapterRepository.findByMangaIdAndChapterNumber(10L, 1d))
                    .thenReturn(Optional.of(new Chapter()));

            Assertions.assertThatThrownBy(() -> chapterService.createChapter(dto))
                    .isInstanceOf(ConflictException.class);

            Mockito.verifyNoInteractions(eventPublisher);
            Mockito.verify(chapterRepository, Mockito.never()).save(ArgumentMatchers.any());
            Mockito.verifyNoInteractions(chapterMapper);
            Mockito.verifyNoInteractions(pageService);
        }
    }

    @Nested
    @DisplayName("updateChapter()")
    class UpdateChapter {

        @Test
        @DisplayName("обновляет главу")
        void shouldUpdateChapter() {
            Long chapterId = 1L;
            ChapterCreateDto request = new ChapterCreateDto();
            request.setTitle("New Title");
            request.setChapterNumber(2d);
            request.setVolumeNumber(1);

            Chapter chapter = Chapter.builder().id(chapterId).title("Old Title").chapterNumber(1d).build();
            Chapter saved = Chapter.builder().id(chapterId).title("New Title").chapterNumber(2d).build();
            ChapterDto expectedDto = new ChapterDto();
            expectedDto.setId(chapterId);

            Mockito.when(chapterRepository.findById(chapterId)).thenReturn(Optional.of(chapter));
            Mockito.when(chapterRepository.save(chapter)).thenReturn(saved);
            Mockito.when(chapterMapper.toDto(saved)).thenReturn(expectedDto);

            ChapterDto result = chapterService.updateChapter(chapterId, request);

            assertThat(result).isEqualTo(expectedDto);
            assertThat(chapter.getTitle()).isEqualTo("New Title");
            assertThat(chapter.getChapterNumber()).isEqualTo(2d);
            assertThat(chapter.getVolumeNumber()).isEqualTo(1);

            Mockito.verify(chapterRepository).save(chapter);
        }

        @Test
        @DisplayName("перезаписывает поля null если они не переданы в request")
        void shouldOverwriteWithNullIfFieldNotProvided() {
            Long chapterId = 1L;
            ChapterCreateDto request = new ChapterCreateDto();
            request.setChapterNumber(2d);

            Chapter chapter = Chapter.builder().id(chapterId).title("Existing Title").chapterNumber(1d).build();
            Chapter saved = Chapter.builder().id(chapterId).title(null).chapterNumber(2d).build();
            ChapterDto expectedDto = new ChapterDto();

            Mockito.when(chapterRepository.findById(chapterId)).thenReturn(Optional.of(chapter));
            Mockito.when(chapterRepository.save(chapter)).thenReturn(saved);
            Mockito.when(chapterMapper.toDto(saved)).thenReturn(expectedDto);

            chapterService.updateChapter(chapterId, request);

            assertThat(chapter.getTitle()).isNull();
            assertThat(chapter.getVolumeNumber()).isNull();
            Mockito.verify(chapterRepository).save(chapter);
        }

        @Test
        @DisplayName("выбрасывает NotFoundException если глава не найдена")
        void shouldThrowNotFoundIfChapterNotFound() {
            Mockito.when(chapterRepository.findById(99L)).thenReturn(Optional.empty());

            Assertions.assertThatThrownBy(() -> chapterService.updateChapter(99L, new ChapterCreateDto()))
                    .isInstanceOf(NotFoundException.class);

            Mockito.verify(chapterRepository, Mockito.never()).save(ArgumentMatchers.any());
        }
    }

    @Nested
    @DisplayName("deleteChapter()")
    class DeleteChapter {

        @Test
        @DisplayName("удаляет главу если существует")
        void shouldDeleteChapter() {
            Long chapterId = 1L;
            Mockito.when(chapterRepository.existsById(chapterId)).thenReturn(true);

            chapterService.deleteChapter(chapterId);

            Mockito.verify(chapterRepository).deleteById(chapterId);
        }

        @Test
        @DisplayName("выбрасывает NotFoundException если глава не найдена")
        void shouldThrowNotFoundIfChapterNotFound() {
            Mockito.when(chapterRepository.existsById(99L)).thenReturn(false);

            Assertions.assertThatThrownBy(() -> chapterService.deleteChapter(99L))
                    .isInstanceOf(NotFoundException.class);

            Mockito.verify(chapterRepository, Mockito.never()).deleteById(ArgumentMatchers.any());
        }
    }

    @Nested
    @DisplayName("getChapterCount()")
    class GetChapterCount {

        @Test
        @DisplayName("возвращает количество глав")
        void shouldReturnChapterCount() {
            Mockito.when(chapterRepository.countByMangaId(10L)).thenReturn(5L);

            long result = chapterService.getChapterCount(10L);

            Assertions.assertThat(result).isEqualTo(5L);
        }

        @Test
        @DisplayName("возвращает 0 если глав нет")
        void shouldReturnZeroIfNoChapters() {
            Mockito.when(chapterRepository.countByMangaId(10L)).thenReturn(0L);

            long result = chapterService.getChapterCount(10L);

            Assertions.assertThat(result).isZero();
        }
    }

    @Nested
    @DisplayName("getChapterListDtoByIds()")
    class GetChapterListDtoByIds {

        @Test
        @DisplayName("возвращает список ChapterListDto по ids")
        void shouldReturnChapterListDtos() {
            Set<Long> ids = Set.of(1L, 2L);
            Chapter c1 = Chapter.builder().id(1L).build();
            Chapter c2 = Chapter.builder().id(2L).build();
            ChapterListDto dto1 = new ChapterListDto();
            ChapterListDto dto2 = new ChapterListDto();

            Mockito.when(chapterRepository.findAllById(ids)).thenReturn(List.of(c1, c2));
            Mockito.when(chapterMapper.toListDto(c1)).thenReturn(dto1);
            Mockito.when(chapterMapper.toListDto(c2)).thenReturn(dto2);

            List<ChapterListDto> result = chapterService.getChapterListDtoByIds(ids);

            Assertions.assertThat(result).containsExactlyInAnyOrder(dto1, dto2);
        }

        @Test
        @DisplayName("возвращает пустой список если ids пустой")
        void shouldReturnEmptyListIfIdsEmpty() {
            Mockito.when(chapterRepository.findAllById(Set.of())).thenReturn(List.of());

            List<ChapterListDto> result = chapterService.getChapterListDtoByIds(Set.of());

            Assertions.assertThat(result).isEmpty();
            Mockito.verifyNoInteractions(chapterMapper);
        }
    }

    @Nested
    @DisplayName("getChaptersByManga()")
    class GetChaptersByManga {

        @Test
        @DisplayName("возвращает страницу глав для манги")
        void shouldReturnPageOfChapters() {
            Long mangaId = 10L;
            Pageable pageable = PageRequest.of(0, 10);

            Chapter chapter = Chapter.builder().id(1L).mangaId(mangaId).chapterNumber(1d).build();
            ChapterListDto listDto = new ChapterListDto();
            listDto.setId(1L);

            Mockito.when(chapterRepository.findByMangaIdOrderByChapterNumberAsc(mangaId, pageable))
                    .thenReturn(new PageImpl<>(List.of(chapter)));
            Mockito.when(chapterMapper.toListDto(chapter)).thenReturn(listDto);

            Page<ChapterListDto> result = chapterService.getChaptersByManga(mangaId, pageable);

            Assertions.assertThat(result.getContent()).containsExactly(listDto);
        }

        @Test
        @DisplayName("возвращает пустую страницу если глав нет")
        void shouldReturnEmptyPageIfNoChapters() {
            Long mangaId = 10L;
            Pageable pageable = PageRequest.of(0, 10);

            Mockito.when(chapterRepository.findByMangaIdOrderByChapterNumberAsc(mangaId, pageable))
                    .thenReturn(Page.empty());

            Page<ChapterListDto> result = chapterService.getChaptersByManga(mangaId, pageable);

            Assertions.assertThat(result.getContent()).isEmpty();
            Mockito.verifyNoInteractions(chapterMapper);
        }
    }
}
