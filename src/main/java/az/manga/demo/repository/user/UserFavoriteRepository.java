package az.manga.demo.repository.user;

import az.manga.demo.model.user.UserFavorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserFavoriteRepository extends JpaRepository<UserFavorite,Long> {

    // Избранное пользователя - с пагинацией!
    Page<UserFavorite> findByUserIdOrderByAddedAtDesc(Long userId, Pageable pageable);

    boolean existsByUserIdAndMangaId(Long userId, Long mangaId);

    Optional<UserFavorite> findByUserIdAndMangaId(Long userId, Long mangaId);

    void deleteByUserIdAndMangaId(Long userId, Long mangaId);

    long countByMangaId(Long mangaId);
}
