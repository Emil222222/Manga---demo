package az.manga.demo.usercontent.history.internal;

import az.manga.demo.common.security.UserPrincipal;
import az.manga.demo.usercontent.history.api.HistoryService;

import az.manga.demo.usercontent.history.api.dto.HistoryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/history")
@RequiredArgsConstructor
public class HistoryController {
    private final HistoryService historyService;

    @GetMapping
    public ResponseEntity<Page<HistoryDto>> getUserHistory(
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(historyService.getHistory(principal.getId(), pageable));
    }

    @DeleteMapping("/{mangaId}")
    public ResponseEntity<Void> deleteHistory(
            @PathVariable Long mangaId,
            @AuthenticationPrincipal UserPrincipal principal){
        historyService.deleteHistory(mangaId, principal.getId());
        return ResponseEntity.ok().build();
    }
}
