package az.manga.demo.usercontent.rating.internal;

import az.manga.demo.common.security.UserPrincipal;
import az.manga.demo.usercontent.rating.internal.dto.RatingCreateDto;
import az.manga.demo.usercontent.rating.api.dto.RatingDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ratings")
@RequiredArgsConstructor
public class RatingController {
    private final RatingServiceImpl ratingService;

    @PostMapping()
    public ResponseEntity<RatingDto> rateOrUpdate(
            @RequestBody @Valid RatingCreateDto request,
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(ratingService.rateOrUpdate(request,user.getId()));
    }

    @DeleteMapping("/{mangaId}")
    public ResponseEntity<Void> deleteRating(
            @PathVariable Long mangaId,
            @AuthenticationPrincipal UserPrincipal user) {
        ratingService.deleteRating(mangaId,user.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{mangaId}/my")
    public ResponseEntity<RatingDto> getMyRatings(
            @PathVariable Long mangaId,
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(ratingService.getUserRating(mangaId,user.getId()));
    }

    @GetMapping("/{mangaId}/average")
    public ResponseEntity<Double> getAverageRating(@PathVariable Long mangaId) {
        return ResponseEntity.ok(ratingService.getAverageRating(mangaId));
    }

    @GetMapping("/{mangaId}/count")
    public ResponseEntity<Long> getVoteCount(@PathVariable Long mangaId) {
        return ResponseEntity.ok(ratingService.getVoteCount(mangaId));
    }

}
