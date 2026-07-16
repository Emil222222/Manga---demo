package az.manga.demo.usercontent.history.api;

import az.manga.demo.usercontent.history.api.dto.HistoryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface HistoryService {
    Page<HistoryDto> getHistory(Long userId,  Pageable pageable);
    void deleteHistory(Long userId, Long mangaId);
}
