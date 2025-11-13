package az.manga.demo.model.content;

import az.manga.demo.model.manga.Manga;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Chapter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manga_id", nullable = false)
    private Manga manga;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private Double chapterNumber; // 1.0, 1.5, 2.0 (может быть дробным)

    private Integer volumeNumber;

    private Integer pageCount = 0;

    @CreatedDate
    private LocalDateTime publishedAt;

    // Связи
    @OneToMany(mappedBy = "chapter", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("pageNumber ASC")
    private List<Page> pages;
}
