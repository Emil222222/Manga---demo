package az.manga.demo.repository.manga;

import org.springframework.data.domain.Page;
import az.manga.demo.model.manga.Comment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findByMangaIdOrderByCreatedAtDesc(Long mangaId, Pageable pageable);

    Page<Comment> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    long countByMangaId(Long mangaId);

    void deleteByIdAndUserId(Long id, Long userId);
}
