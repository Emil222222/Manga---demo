package az.manga.demo.usercontent.history.internal;

import az.manga.demo.common.event.ChapterReadEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
@RequiredArgsConstructor
public class HistoryListener {

    private final HistoryRepository historyRepository;

    @Async
    @EventListener
    @Transactional
    public void onChapterReady(ChapterReadEvent event) {
        log.info("Updating history for user id = {}, manga id = {}",
                event.userId(), event.mangaId());

        ReadingHistory readingHistory = historyRepository
                .findByUserIdAndMangaId(event.userId(), event.mangaId())
                .orElse(ReadingHistory.builder()
                        .userId(event.userId())
                        .mangaId(event.mangaId())
                        .build());

        readingHistory.setLastReadChapterId(event.chapterId());
        readingHistory.setLastReadPage(event.lastReadPage());
        historyRepository.save(readingHistory);
    }

}
