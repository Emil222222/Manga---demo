package az.manga.demo.usercontent.history.api.dto;

import az.manga.demo.manga.chapter.api.dto.ChapterListDto;
import az.manga.demo.manga.manga.api.dto.MangaListDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HistoryDto {
    private Long id;
    private Long userId;
    private MangaListDto manga;
    private ChapterListDto chapter;
    private Integer lastReadPage;
    private LocalDateTime lastReadTime;
}
