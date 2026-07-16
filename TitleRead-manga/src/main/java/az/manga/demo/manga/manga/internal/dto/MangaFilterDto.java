package az.manga.demo.manga.manga.internal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MangaFilterDto {
    private String title;
    private String author;
    private Set<Long> genreIds;
    private Set<Long> tagIds;
    private String status;
    private String type;
    private Double minRating;
    private Double maxRating;
}
