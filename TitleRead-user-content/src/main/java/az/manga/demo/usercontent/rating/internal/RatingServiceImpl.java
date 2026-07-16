package az.manga.demo.usercontent.rating.internal;

import az.manga.demo.common.exception.ErrorMessage;
import az.manga.demo.common.exception.NotFoundException;
import az.manga.demo.usercontent.rating.api.RatingService;
import az.manga.demo.usercontent.rating.internal.dto.RatingCreateDto;
import az.manga.demo.usercontent.rating.api.dto.RatingDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RatingServiceImpl implements RatingService {

    private final RatingRepository ratingRepository;
    public final RatingMapper ratingMapper;

    @Override
    @Caching(evict = {
            @CacheEvict(value = "ratings:average", key = "#request.mangaId"),
            @CacheEvict(value = "ratings:count", key = "#request.mangaId")
    })
    public RatingDto rateOrUpdate(RatingCreateDto request, Long userId) {
        log.debug("Rating manga id = {} by user id = {}", request.getMangaId(), userId);

        Rating rating = ratingRepository
                .findByUserIdAndMangaId(userId, request.getMangaId())
                .orElseGet(() -> {
                    Rating r = ratingMapper.toEntity(request);
                    r.setUserId(userId);
                    return r;
                });

        rating.setScore(request.getScore());

        return  ratingMapper.toDto(ratingRepository.save(rating));
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "ratings:average", key = "#mangaId"),
            @CacheEvict(value = "ratings:count", key = "#mangaId")
    })
    public void deleteRating(Long mangaId, Long userId) {
        log.debug("Deleting rating for manga id = {} by user id = {}", mangaId, userId);
        if (!ratingRepository.existsByUserIdAndMangaId(userId, mangaId)) {
            throw new NotFoundException(ErrorMessage.MANGA_NOT_FOUND);
        }
        ratingRepository.deleteByUserIdAndMangaId(userId, mangaId);
    }

    @Override
    @Transactional(readOnly = true)
    public RatingDto getUserRating(Long mangaId, Long userId) {
        log.debug("Fetching rating for manga id = {} by user id = {}", mangaId, userId);
        return ratingMapper.toDto(ratingRepository.findByUserIdAndMangaId(userId , mangaId)
                .orElseThrow(()-> new NotFoundException(ErrorMessage.MANGA_NOT_FOUND)));
    }

    @Override
    @Cacheable(value = "ratings:average", key = "#mangaId")
    @Transactional(readOnly = true)
    public double getAverageRating(Long mangaId) {
        log.debug("Fetching average rating for manga id = {}", mangaId);
        return ratingRepository.calculateAverageRating(mangaId);
    }

    @Override
    @Cacheable(value = "ratings:count", key = "#mangaId")
    @Transactional(readOnly = true)
    public Long getVoteCount(Long mangaId){
        log.debug("Fetching vote count for manga id = {}", mangaId);
        return ratingRepository.countByMangaId(mangaId);
    }
}
