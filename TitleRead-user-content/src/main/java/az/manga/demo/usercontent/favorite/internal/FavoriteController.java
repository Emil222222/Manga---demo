package az.manga.demo.usercontent.favorite.internal;

import az.manga.demo.common.security.UserPrincipal;
import az.manga.demo.usercontent.favorite.api.FavoriteService;
import az.manga.demo.usercontent.favorite.api.dto.FavoriteDto;
import az.manga.demo.usercontent.favorite.internal.dto.FavoriteCreateDto;
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
@RequestMapping("/v1/favorite")
public class FavoriteController {
    private final FavoriteService favoriteService;

    @GetMapping
    public ResponseEntity<Page<FavoriteDto>> getFavorites(
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(favoriteService.getUserFavorites(principal.getId(), pageable));
    }

    @GetMapping("/check/{mangaId}")
    public ResponseEntity<Boolean> isFavorite(
            @PathVariable Long mangaId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(favoriteService.isFavorite(mangaId, principal.getId()));
    }

    @GetMapping("/count/{mangaId}")
    public ResponseEntity<Long> countByManga(@PathVariable Long mangaId) {
        return ResponseEntity.ok(favoriteService.countByManga(mangaId));
    }

    @PostMapping
    public ResponseEntity<FavoriteDto> addFavorite(
            @Valid @RequestBody FavoriteCreateDto dto,
            @AuthenticationPrincipal UserPrincipal principal
            ){
        return ResponseEntity.status(HttpStatus.CREATED).body(favoriteService.add(principal.getId(),dto));
    }

    @DeleteMapping("/{mangaId}")
    public ResponseEntity<FavoriteDto> deleteFavorite(
            @PathVariable Long mangaId,
            @AuthenticationPrincipal UserPrincipal principal
    ){
        favoriteService.remove(principal.getId(),mangaId);
        return ResponseEntity.noContent().build();
    }

}
