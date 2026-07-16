package az.manga.demo.usercontent.favorite.internal.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FavoriteCreateDto {

    @NotNull(message = "Manga ID is required")
    private Long mangaId;
}
