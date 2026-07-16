package az.manga.demo.manga.page.internal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PageCreateDto {
    @NotNull
    private Long chapterId;

    @NotNull
    private Integer pageNumber;

    @NotBlank
    private String imageUrl;

    private Integer width;
    private Integer height;
}