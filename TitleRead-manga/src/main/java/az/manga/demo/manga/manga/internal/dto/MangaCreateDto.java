package az.manga.demo.manga.manga.internal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MangaCreateDto {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Slug is required")
    private String slug;

    private String description;

    private String author;

    private String artist;

    @NotNull(message = "Status is required")
    private String status;

    @NotNull(message = "Type is required")
    private String type;

    private String coverImageUrl;

    private Set<Long> genreIds;

    private Set<Long> tagIds;
}