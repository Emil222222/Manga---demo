package az.manga.demo.common.event;

public record ChapterReadEvent(
        Long mangaId,
        Long chapterId,
        Long userId,
        Integer lastReadPage
) {}