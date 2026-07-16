package az.manga.demo.manga.manga.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MangaListDto {

    private Long id;

    private String title;

    private String slug;

    private String status;

    private String type;

    private String coverImageUrl;

    private String author;

    private Double averageRating;

    private Integer totalChapters;

    private Set<String> genreNames;

    private Double latestChapterNumber;
}
