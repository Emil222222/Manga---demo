package az.manga.demo.usercontent.history.internal;

import az.manga.demo.common.exception.ErrorMessage;
import az.manga.demo.common.exception.NotFoundException;
import az.manga.demo.manga.chapter.api.ChapterService;
import az.manga.demo.manga.chapter.api.dto.ChapterListDto;
import az.manga.demo.manga.manga.api.MangaService;
import az.manga.demo.manga.manga.api.dto.MangaListDto;
import az.manga.demo.usercontent.history.api.HistoryService;
import az.manga.demo.usercontent.history.api.dto.HistoryDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class HistoryServiceImpl implements HistoryService {
    private final HistoryRepository historyRepository;
    private final HistoryMapper historyMapper;
    private final MangaService mangaService;
    private final ChapterService chapterService;

    @Override
    @Transactional(readOnly = true)
    public Page<HistoryDto> getHistory(Long userId ,Pageable pageable) {
        log.debug("Fetching history for user id = {}", userId);
        Page<ReadingHistory> history = historyRepository
                .findByUserIdOrderByLastReadAtDesc(userId, pageable);

        Set<Long> mangaIds = history.stream()
                .map(ReadingHistory::getMangaId)
                .collect(Collectors.toSet());

        Map<Long, MangaListDto> mangaMap = mangaService.getMangaListByIds(mangaIds).stream()
                .collect(Collectors.toMap(MangaListDto::getId, manga -> manga));

        Set<Long> chapterIds = history.stream()
                .map(ReadingHistory::getLastReadChapterId)
                .collect(Collectors.toSet());

        Map<Long, ChapterListDto> chapterMap = chapterService.getChapterListDtoByIds(chapterIds).stream()
                .collect(Collectors.toMap(ChapterListDto::getId, chapter -> chapter));

        return history.map(h -> {
            HistoryDto dto = historyMapper.toDto(h);
            dto.setManga(mangaMap.get(h.getMangaId()));
            dto.setChapter(chapterMap.get(h.getLastReadChapterId()));
            return dto;
        });
    }

    @Override
    public void deleteHistory(Long userId, Long mangaId) {
        log.info("Deleting history for user id = {}, mangaId = {}",userId, mangaId);
        historyRepository.delete(historyRepository.findByUserIdAndMangaId(userId,mangaId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.HISTORY_NOT_FOUND)));
    }
}
