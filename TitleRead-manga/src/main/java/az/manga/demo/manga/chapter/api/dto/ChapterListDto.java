package az.manga.demo.manga.chapter.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChapterListDto {

    private Long id;

    private String title;

    private Double chapterNumber;

    private Integer volumeNumber;

    private Integer pageCount;

    private LocalDateTime publishedAt;
}
