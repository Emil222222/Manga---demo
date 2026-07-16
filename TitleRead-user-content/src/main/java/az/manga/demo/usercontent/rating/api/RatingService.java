package az.manga.demo.usercontent.rating.api;

import az.manga.demo.usercontent.rating.api.dto.RatingDto;
import az.manga.demo.usercontent.rating.internal.dto.RatingCreateDto;

public interface RatingService {
    RatingDto rateOrUpdate(RatingCreateDto request, Long userId);
    void deleteRating(Long mangaId, Long userId);
    RatingDto getUserRating(Long mangaId, Long userId);
    double getAverageRating(Long mangaId);
    Long getVoteCount(Long mangaId);
}
