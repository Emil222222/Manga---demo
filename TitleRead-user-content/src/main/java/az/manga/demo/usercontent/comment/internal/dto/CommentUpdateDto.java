package az.manga.demo.usercontent.comment.internal.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentUpdateDto {
    @NotBlank(message = "Comment cannot be empty")
    private String content;
}
