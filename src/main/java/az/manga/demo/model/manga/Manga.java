package az.manga.demo.model.manga;

import az.manga.demo.enums.MangaStatus;
import az.manga.demo.enums.MangaType;
import az.manga.demo.model.content.Chapter;
import az.manga.demo.model.user.UserFavorite;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Manga {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String author;

    @Enumerated(EnumType.STRING)
    private MangaStatus status;

    @Enumerated(EnumType.STRING)
    private MangaType type;

    private String coverImageUrl;

    private Integer totalChapters = 0;
    private Double averageRating = 0.0;
    private Integer viewCount = 0;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @ManyToMany
    @JoinTable(
            name = "manga_genres",
            joinColumns = @JoinColumn(name = "manga_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    private Set<Genre> genres = new HashSet<>();

    // Теги - ManyToMany
    @ManyToMany
    @JoinTable(
            name = "manga_tags",
            joinColumns = @JoinColumn(name = "manga_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    // Остальные связи
    @OneToMany(mappedBy = "manga", cascade = CascadeType.ALL)
    private List<Chapter> chapters;

    @OneToMany(mappedBy = "manga", cascade = CascadeType.ALL)
    private List<UserFavorite> favorites;

    @OneToMany(mappedBy = "manga", cascade = CascadeType.ALL)
    private List<Rating> ratings;

    @OneToMany(mappedBy = "manga", cascade = CascadeType.ALL)
    private List<Comment> comments;

}
