package az.manga.demo.manga.chapter.internal.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChapterCreateDto {

    @NotNull(message = "Manga ID is required")
    private Long mangaId;

    @NotBlank(message = "Title is required")
    private String title;

    @NotNull(message = "Chapter number is required")
    @Min(value = 0, message = "Chapter number must be positive")
    private Double chapterNumber;

    private Integer volumeNumber;

    private List<String> pageImageUrls;
}
