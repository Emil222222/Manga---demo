package az.manga.demo.usercontent.comment.api;

import az.manga.demo.usercontent.comment.api.dto.CommentDto;
import az.manga.demo.usercontent.comment.internal.dto.CommentCreateDto;
import az.manga.demo.usercontent.comment.internal.dto.CommentUpdateDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommentService {
    CommentDto create(Long userId, CommentCreateDto dto);
    Page<CommentDto> getByManga(Long mangaId, Pageable pageable);
    Page<CommentDto> getByUser(Long userId, Pageable pageable);
    CommentDto update(Long commentId, Long userId, CommentUpdateDto dto);
    void delete(Long commentId, Long mangaId, Long userId);
    long countByManga(Long mangaId);
}
