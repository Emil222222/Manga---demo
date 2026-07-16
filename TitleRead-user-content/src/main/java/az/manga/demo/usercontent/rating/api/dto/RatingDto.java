package az.manga.demo.usercontent.rating.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RatingDto {
    private Long id;

    private Long mangaId;

    private Long userId;

    private Integer score;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
