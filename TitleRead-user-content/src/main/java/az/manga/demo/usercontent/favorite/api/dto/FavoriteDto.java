package az.manga.demo.usercontent.favorite.api.dto;

import az.manga.demo.manga.manga.api.dto.MangaListDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FavoriteDto {
    private Long id;
    private Long userId;
    private MangaListDto mangaListDto;
    private LocalDateTime addedAt;
}
