package az.manga.demo.usercontent.favorite.internal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FavoriteRepository extends JpaRepository<UserFavorite,Long> {

    Page<UserFavorite> findByUserIdOrderByAddedAtDesc(Long userId, Pageable pageable);

    boolean existsByUserIdAndMangaId(Long userId, Long mangaId);

    void deleteByUserIdAndMangaId(Long userId, Long mangaId);

    long countByMangaId(Long mangaId);
}