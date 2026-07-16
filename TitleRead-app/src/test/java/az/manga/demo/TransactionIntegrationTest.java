package az.manga.demo;

import az.manga.demo.manga.chapter.api.ChapterService;
import az.manga.demo.manga.chapter.internal.ChapterRepository;
import az.manga.demo.manga.chapter.internal.dto.ChapterCreateDto;
import az.manga.demo.manga.page.api.PageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;

import java.util.List;


public class TransactionIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ChapterService chapterService;

    @Autowired
    private ChapterRepository chapterRepository;

    @MockitoBean
    private PageService pageService;

    @Test
    @DisplayName("createChapter rolls back saved chapter when addPages fails")
    void shouldRollbackChapterWhenAddPagesFails() {
        ChapterCreateDto dto = new ChapterCreateDto();
        dto.setMangaId(100L);
        dto.setChapterNumber(1d);
        dto.setTitle("Test Chapter");
        dto.setPageImageUrls(List.of("url1", "url2"));

        doThrow(new RuntimeException("Simulated failure"))
                .when(pageService).addPages(any(), anyList());

        assertThatThrownBy(() -> chapterService.createChapter(dto))
                .isInstanceOf(RuntimeException.class);

        boolean exists = chapterRepository
                .findByMangaIdAndChapterNumber(100L,1d)
                        .isPresent();

        assertThat(exists).isFalse();
    }
}