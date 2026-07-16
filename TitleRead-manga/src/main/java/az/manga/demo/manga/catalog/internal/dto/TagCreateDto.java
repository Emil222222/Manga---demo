package az.manga.demo.manga.catalog.internal.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TagCreateDto {
    @NotBlank(message = "Genre slug is required")
    private String slug;

    @NotBlank(message = "Genre name is required")
    private String name;
}
