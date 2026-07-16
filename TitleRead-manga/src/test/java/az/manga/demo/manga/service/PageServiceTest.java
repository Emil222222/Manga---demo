package az.manga.demo.manga.service;

import az.manga.demo.common.exception.NotFoundException;
import az.manga.demo.page.api.dto.PageDto;
import az.manga.demo.page.internal.Page;
import az.manga.demo.page.internal.PageMapper;
import az.manga.demo.page.internal.PageRepository;
import az.manga.demo.page.internal.PageServiceImpl;
import az.manga.demo.page.internal.dto.PageCreateDto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PageServiceTest {

    @Mock private PageRepository pageRepository;
    @Mock private PageMapper pageMapper;

    @InjectMocks
    private PageServiceImpl pageService;

    private Page buildPage(Long id, Long chapterId, int pageNumber) {
        return Page.builder()
                .id(id)
                .chapterId(chapterId)
                .pageNumber(pageNumber)
                .imageUrl("https://test.com/page" + pageNumber + ".jpg")
                .width(800)
                .height(1200)
                .build();
    }

    private PageDto buildPageDto(Long id, int pageNumber) {
        return new PageDto(id,
                pageNumber,
                "https://test.com/page" + pageNumber + ".jpg",
                800,
                1200);
    }

    @Nested
    @DisplayName("getPagesByChapter()")
    class GetPagesByChapter {

        @Test
        @DisplayName("возвращает список страниц главы")
        void shouldReturnPages() {
            Long chapterId = 1L;
            Page page1 = buildPage(1L, chapterId, 1);
            Page page2 = buildPage(2L, chapterId, 2);
            PageDto dto1 = buildPageDto(1L, 1);
            PageDto dto2 = buildPageDto(2L, 2);

            when(pageRepository.findByChapterIdOrderByPageNumberAsc(chapterId))
                    .thenReturn(List.of(page1, page2));
            when(pageMapper.toDto(page1)).thenReturn(dto1);
            when(pageMapper.toDto(page2)).thenReturn(dto2);

            List<PageDto> result = pageService.getPagesByChapter(chapterId);

            assertThat(result).hasSize(2);
            assertThat(result).containsExactly(dto1, dto2); // проверяем содержимое и порядок
            verify(pageMapper).toDto(page1);
            verify(pageMapper).toDto(page2);
        }

        @Test
        @DisplayName("возвращает пустой список если страниц нет")
        void shouldReturnEmptyListIfNoPages() {
            Long chapterId = 1L;
            when(pageRepository.findByChapterIdOrderByPageNumberAsc(chapterId))
                    .thenReturn(List.of());

            List<PageDto> result = pageService.getPagesByChapter(chapterId);

            assertThat(result).isEmpty();

            verifyNoInteractions(pageMapper);
        }
    }

    @Nested
    @DisplayName("addPage()")
    class AddPage {

        @Test
        @DisplayName("добавляет страницу в главу")
        void shouldAddPage() {
            PageCreateDto dto = new PageCreateDto();
            dto.setChapterId(1L);
            dto.setPageNumber(1);
            dto.setImageUrl("https://test.com/page1.jpg");

            Page page = buildPage(null, 1L, 1);
            Page saved = buildPage(1L, 1L, 1);
            PageDto expectedDto = buildPageDto(1L, 1);

            when(pageMapper.toEntity(dto)).thenReturn(page);
            when(pageRepository.save(page)).thenReturn(saved);
            when(pageMapper.toDto(saved)).thenReturn(expectedDto);

            PageDto result = pageService.addPage(dto);

            assertThat(result).isEqualTo(expectedDto);

            ArgumentCaptor<Page> captor = ArgumentCaptor.forClass(Page.class);
            verify(pageRepository).save(captor.capture());

            Page capturedPage = captor.getValue();
            assertThat(capturedPage.getChapterId()).isEqualTo(1L);
            assertThat(capturedPage.getPageNumber()).isEqualTo(1);

            verify(pageMapper).toEntity(dto);
            verify(pageMapper).toDto(saved);
        }
    }

    @Nested
    @DisplayName("updatePage()")
    class UpdatePage {

        @Test
        @DisplayName("обновляет страницу")
        void shouldUpdatePage() {
            Long pageId = 1L;
            PageCreateDto dto = new PageCreateDto();
            dto.setChapterId(1L);
            dto.setPageNumber(2);
            dto.setImageUrl("new-url");
            dto.setWidth(800);
            dto.setHeight(1200);

            Page page = buildPage(pageId, 1L, 1);
            page.setImageUrl("old-url");

            Page saved = buildPage(pageId, 1L, 2);
            saved.setImageUrl("new-url");
            saved.setWidth(800);
            saved.setHeight(1200);

            PageDto expectedDto = buildPageDto(pageId, 2);

            when(pageRepository.findById(pageId)).thenReturn(Optional.of(page));
            when(pageRepository.save(page)).thenReturn(saved);
            when(pageMapper.toDto(saved)).thenReturn(expectedDto);

            PageDto result = pageService.updatePage(pageId, dto);

            assertThat(result).isEqualTo(expectedDto);
            assertThat(page.getPageNumber()).isEqualTo(2);
            assertThat(page.getImageUrl()).isEqualTo("new-url");
            assertThat(page.getWidth()).isEqualTo(800);
            assertThat(page.getHeight()).isEqualTo(1200);

            verify(pageRepository).save(page);
            verify(pageMapper).toDto(saved);
        }

        @Test
        @DisplayName("выбрасывает NotFoundException если страница не найдена")
        void shouldThrowNotFoundIfPageNotFound() {
            when(pageRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> pageService.updatePage(99L, new PageCreateDto()))
                    .isInstanceOf(NotFoundException.class);

            verify(pageRepository, never()).save(any());
            verifyNoInteractions(pageMapper);
        }
    }

    @Nested
    @DisplayName("deletePage()")
    class DeletePage {

        @Test
        @DisplayName("удаляет страницу если существует")
        void shouldDeletePage() {
            when(pageRepository.existsById(1L)).thenReturn(true);

            pageService.deletePage(1L);

            verify(pageRepository).deleteById(1L);
        }

        @Test
        @DisplayName("выбрасывает NotFoundException если страница не найдена")
        void shouldThrowNotFoundIfPageNotFound() {
            when(pageRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> pageService.deletePage(99L))
                    .isInstanceOf(NotFoundException.class);

            verify(pageRepository, never()).deleteById(any());
        }
    }

    @Nested
    @DisplayName("addPages()")
    class AddPages {

        @Test
        @DisplayName("сохраняет все страницы с правильными номерами")
        @SuppressWarnings("unchecked")
        void shouldSaveAllPagesWithCorrectNumbers() {
            Long chapterId = 1L;
            List<String> urls = List.of("url1", "url2", "url3");

            pageService.addPages(chapterId, urls);

            ArgumentCaptor<List<Page>> captor = ArgumentCaptor.forClass(List.class);
            verify(pageRepository).saveAll(captor.capture());

            List<Page> saved = captor.getValue();
            assertThat(saved).hasSize(3);
            assertThat(saved.get(0)).extracting(Page::getPageNumber, Page::getImageUrl, Page::getChapterId)
                    .containsExactly(1, "url1", chapterId);
            assertThat(saved.get(1)).extracting(Page::getPageNumber, Page::getImageUrl, Page::getChapterId)
                    .containsExactly(2, "url2", chapterId);
            assertThat(saved.get(2)).extracting(Page::getPageNumber, Page::getImageUrl, Page::getChapterId)
                    .containsExactly(3, "url3", chapterId);


        }

        @Test
        @DisplayName("ничего не сохраняет если список пустой")
        @SuppressWarnings("unchecked")
        void shouldSaveEmptyListIfNoUrls() {
            pageService.addPages(1L, List.of());

            ArgumentCaptor<List<Page>> captor = ArgumentCaptor.forClass(List.class);
            verify(pageRepository).saveAll(captor.capture());

            assertThat(captor.getValue()).isEmpty();
        }
    }
}