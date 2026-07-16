package az.manga.demo.usercontent.comment.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentDto {
    private Long id;

    private Long mangaId;

    private Long userId;

    private String username;

    private String userAvatarUrl;

    private String content;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
