package az.manga.demo.manga.chapter.internal;

import az.manga.demo.manga.chapter.api.ChapterService;
import az.manga.demo.manga.chapter.api.dto.ChapterDto;
import az.manga.demo.manga.chapter.api.dto.ChapterListDto;
import az.manga.demo.manga.chapter.internal.dto.ChapterCreateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/chapter")
@RequiredArgsConstructor
public class ChapterController {

    private final ChapterService chapterService;


    @GetMapping("/{id}")
    public ResponseEntity<ChapterDto> getChapter(
            @PathVariable Long id,
            @RequestParam(required = false) Long userId
    ) {
        return ResponseEntity.ok(chapterService.getChapter(id, userId));
    }

    @GetMapping("/manga/{mangaId}")
    public ResponseEntity<Page<ChapterListDto>> getChaptersByManga(
            @PathVariable Long mangaId,
            @PageableDefault(size = 20, sort = "chapterNumber") Pageable pageable
    ) {
        return ResponseEntity.ok(chapterService.getChaptersByManga(mangaId, pageable));
    }

    @GetMapping("/manga/{mangaId}/count")
    public ResponseEntity<Long> getChapterCount(@PathVariable Long mangaId) {
        return ResponseEntity.ok(chapterService.getChapterCount(mangaId));
    }

    @PostMapping
    public ResponseEntity<ChapterDto> createChapter(@RequestBody ChapterCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(chapterService.createChapter(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ChapterDto> updateChapter(
            @PathVariable Long id,
            @RequestBody ChapterCreateDto dto
    ) {
        return ResponseEntity.ok(chapterService.updateChapter(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteChapter(@PathVariable Long id) {
        chapterService.deleteChapter(id);
        return ResponseEntity.noContent().build();
    }
}
