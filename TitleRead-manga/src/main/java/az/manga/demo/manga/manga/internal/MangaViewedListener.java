package az.manga.demo.manga.manga.internal;

import az.manga.demo.common.event.MangaViewedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Component
public class MangaViewedListener {

    private final MangaRepository mangaRepository;

    @Async
    @EventListener
    @Transactional
    public void onViewed(MangaViewedEvent event) {
        log.debug("Manga {} viewed by userId = {}", event.mangaId(), event.userId());
        mangaRepository.incrementViewCount(event.mangaId());
    }
}
