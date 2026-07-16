package az.manga.demo.usercontent.comment.internal.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentCreateDto {
    @NotNull(message = "Manga ID is required")
    private Long mangaId;

    @NotNull(message = "Comment cannot be empty")
    private String content;
}
