package az.manga.demo.manga.page.internal;

import az.manga.demo.manga.page.internal.dto.PageCreateDto;
import az.manga.demo.manga.page.api.dto.PageDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pages")
@RequiredArgsConstructor
public class PageController {
    private final PageServiceImpl pageService;

    @GetMapping("/chapter/{chapterId}")
    public ResponseEntity<List<PageDto>> getPagesByChapter(@PathVariable Long chapterId) {
        return ResponseEntity.ok(pageService.getPagesByChapter(chapterId));
    }

    @PostMapping()
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<PageDto> addPage(@RequestBody @Valid PageCreateDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pageService.addPage(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<PageDto> updatePage(
            @PathVariable Long id,
            @RequestBody @Valid PageCreateDto request) {
        return ResponseEntity.ok(pageService.updatePage(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<Void> deletePage(@PathVariable Long id) {
        pageService.deletePage(id);
        return ResponseEntity.noContent().build();
    }
}
