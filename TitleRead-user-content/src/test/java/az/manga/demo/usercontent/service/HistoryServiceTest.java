package az.manga.demo.usercontent.service;


import az.manga.demo.common.exception.NotFoundException;
import az.manga.demo.manga.chapter.api.ChapterService;
import az.manga.demo.manga.chapter.api.dto.ChapterListDto;
import az.manga.demo.manga.manga.api.MangaService;
import az.manga.demo.manga.manga.api.dto.MangaListDto;
import az.manga.demo.usercontent.history.api.dto.HistoryDto;
import az.manga.demo.usercontent.history.internal.HistoryMapper;
import az.manga.demo.usercontent.history.internal.HistoryRepository;
import az.manga.demo.usercontent.history.internal.HistoryServiceImpl;
import az.manga.demo.usercontent.history.internal.ReadingHistory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HistoryServiceTest {

    @Mock private HistoryRepository historyRepository;
    @Mock private HistoryMapper historyMapper;
    @Mock private MangaService mangaService;
    @Mock private ChapterService chapterService;

    @InjectMocks
    private HistoryServiceImpl historyService;

    @Nested
    @DisplayName("getHistory()")
    class GetHistory {

        @Test
        @DisplayName("возвращает историю с данными манги и главы")
        void shouldReturnHistoryWithMangaAndChapter() {
            Long userId = 1L;
            Pageable pageable = PageRequest.of(0, 10);

            ReadingHistory history = new ReadingHistory();
            history.setUserId(userId);
            history.setMangaId(10L);
            history.setLastReadChapterId(5L);

            Page<ReadingHistory> historyPage = new PageImpl<>(List.of(history));

            MangaListDto mangaDto = new MangaListDto();
            mangaDto.setId(10L);

            ChapterListDto chapterDto = new ChapterListDto();
            chapterDto.setId(5L);

            HistoryDto historyDto = new HistoryDto();
            historyDto.setManga(null);
            historyDto.setChapter(null);

            when(historyRepository.findByUserIdOrderByLastReadAtDesc(userId, pageable))
                    .thenReturn(historyPage);
            when(mangaService.getMangaListByIds(Set.of(10L))).thenReturn(List.of(mangaDto));
            when(chapterService.getChapterListDtoByIds(Set.of(5L))).thenReturn(List.of(chapterDto));
            when(historyMapper.toDto(history)).thenReturn(historyDto);

            Page<HistoryDto> result = historyService.getHistory(userId, pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(historyDto.getManga()).isEqualTo(mangaDto);
            assertThat(historyDto.getChapter()).isEqualTo(chapterDto);
        }

        @Test
        @DisplayName("возвращает пустую страницу если истории нет")
        void shouldReturnEmptyPageIfNoHistory() {
            Long userId = 1L;
            Pageable pageable = PageRequest.of(0, 10);

            when(historyRepository.findByUserIdOrderByLastReadAtDesc(userId, pageable))
                    .thenReturn(Page.empty());
            when(mangaService.getMangaListByIds(Set.of())).thenReturn(List.of());
            when(chapterService.getChapterListDtoByIds(Set.of())).thenReturn(List.of());

            Page<HistoryDto> result = historyService.getHistory(userId, pageable);

            assertThat(result.getContent()).isEmpty();
            verify(historyMapper, never()).toDto(any());
        }

        @Test
        @DisplayName("корректно обрабатывает несколько записей истории")
        void shouldHandleMultipleHistoryEntries() {
            Long userId = 1L;
            Pageable pageable = PageRequest.of(0, 10);

            ReadingHistory h1 = new ReadingHistory();
            h1.setMangaId(10L);
            h1.setLastReadChapterId(5L);

            ReadingHistory h2 = new ReadingHistory();
            h2.setMangaId(20L);
            h2.setLastReadChapterId(8L);

            MangaListDto manga1 = new MangaListDto(); manga1.setId(10L);
            MangaListDto manga2 = new MangaListDto(); manga2.setId(20L);
            ChapterListDto chapter1 = new ChapterListDto(); chapter1.setId(5L);
            ChapterListDto chapter2 = new ChapterListDto(); chapter2.setId(8L);
            HistoryDto dto1 = new HistoryDto();
            HistoryDto dto2 = new HistoryDto();

            when(historyRepository.findByUserIdOrderByLastReadAtDesc(userId, pageable))
                    .thenReturn(new PageImpl<>(List.of(h1, h2)));
            when(mangaService.getMangaListByIds(Set.of(10L, 20L)))
                    .thenReturn(List.of(manga1, manga2));
            when(chapterService.getChapterListDtoByIds(Set.of(5L, 8L)))
                    .thenReturn(List.of(chapter1, chapter2));
            when(historyMapper.toDto(h1)).thenReturn(dto1);
            when(historyMapper.toDto(h2)).thenReturn(dto2);

            Page<HistoryDto> result = historyService.getHistory(userId, pageable);

            assertThat(result.getContent()).hasSize(2);

            assertThat(dto1.getManga()).isEqualTo(manga1);
            assertThat(dto1.getChapter()).isEqualTo(chapter1);
            assertThat(dto2.getManga()).isEqualTo(manga2);
            assertThat(dto2.getChapter()).isEqualTo(chapter2);

            verify(mangaService, times(1)).getMangaListByIds(any());
            verify(chapterService, times(1)).getChapterListDtoByIds(any());
        }
    }

    @Nested
    @DisplayName("deleteHistory()")
    class DeleteHistory {

        @Test
        @DisplayName("удаляет запись истории")
        void shouldDeleteHistory() {
            Long userId = 1L;
            Long mangaId = 10L;
            ReadingHistory history = new ReadingHistory();
            history.setUserId(userId);
            history.setMangaId(mangaId);

            when(historyRepository.findByUserIdAndMangaId(userId, mangaId))
                    .thenReturn(Optional.of(history));

            historyService.deleteHistory(userId, mangaId);

            verify(historyRepository).delete(history);
        }

        @Test
        @DisplayName("выбрасывает NotFoundException если история не найдена")
        void shouldThrowNotFoundIfHistoryNotFound() {
            Long userId = 1L;
            Long mangaId = 99L;

            when(historyRepository.findByUserIdAndMangaId(userId, mangaId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> historyService.deleteHistory(userId, mangaId))
                    .isInstanceOf(NotFoundException.class);

            verify(historyRepository, never()).delete(any());
        }
    }
}