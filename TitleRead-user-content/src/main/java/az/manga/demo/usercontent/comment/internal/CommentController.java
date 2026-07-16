package az.manga.demo.usercontent.comment.internal;

import az.manga.demo.usercontent.comment.api.CommentService;
import az.manga.demo.usercontent.comment.api.dto.CommentDto;
import az.manga.demo.usercontent.comment.internal.dto.CommentCreateDto;
import az.manga.demo.usercontent.comment.internal.dto.CommentUpdateDto;
import az.manga.demo.common.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/comments")
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/manga/{mangaId}")
    public ResponseEntity<Page<CommentDto>> getByManga(
            @PathVariable Long mangaId,
            @PageableDefault(size = 20) Pageable pageable) {
       return ResponseEntity.ok(commentService.getByManga(mangaId, pageable));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<CommentDto>> getByUser(
            @PathVariable Long userId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(commentService.getByUser(userId, pageable));
    }

    @GetMapping("/manga/{mangaId}/count")
    public ResponseEntity<Long> countByManga(@PathVariable Long mangaId) {
        return ResponseEntity.ok(commentService.countByManga(mangaId));
    }

    @PostMapping
    public ResponseEntity<CommentDto> create(
            @Valid @RequestBody CommentCreateDto dto,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.create(principal.getId(), dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommentDto> update(
            @PathVariable Long id,
            @Valid @RequestBody CommentUpdateDto dto,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(commentService.update(id, principal.getId(), dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @RequestParam Long mangaId,
            @AuthenticationPrincipal UserPrincipal principal) {
        commentService.delete(id, mangaId, principal.getId());
        return ResponseEntity.noContent().build();
    }

}
