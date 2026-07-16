package az.manga.demo.manga.manga.internal;

import az.manga.demo.common.security.UserPrincipal;

import az.manga.demo.manga.manga.api.MangaService;
import az.manga.demo.manga.manga.api.dto.MangaDto;
import az.manga.demo.manga.manga.api.dto.MangaListDto;
import az.manga.demo.manga.manga.internal.dto.MangaCreateDto;
import az.manga.demo.manga.manga.internal.dto.MangaFilterDto;
import az.manga.demo.manga.manga.internal.dto.MangaUpdateDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/v1/manga")
@RequiredArgsConstructor
public class MangaController {

    private final MangaService mangaService;

    @GetMapping
    public ResponseEntity<Page<MangaListDto>> getAllManga(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(mangaService.getAll(pageable));
    }

    @GetMapping("/filter")
    public ResponseEntity<Page<MangaListDto>> filter(
            MangaFilterDto dto,
            @PageableDefault(size = 20) Pageable pageable
    ){
        return ResponseEntity.ok(mangaService.filter(dto, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MangaDto> getMangaById(@PathVariable Long id) {
        return ResponseEntity.ok(mangaService.getById(id));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<MangaDto> getMangaBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(mangaService.getBySlug(slug));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MangaDto> createManga(@Valid @RequestBody MangaCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mangaService.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MangaDto> updateManga(
            @PathVariable Long id,
            @Valid @RequestBody MangaUpdateDto dto) {
        return  ResponseEntity.ok(mangaService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MangaDto> deleteMangaById(@PathVariable Long id) {
        mangaService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/view")
    public ResponseEntity<Void> incrementView(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal
    ){
        mangaService.registerView(id, principal != null ? principal.getId() : null);
        return ResponseEntity.noContent().build();
    }
}
