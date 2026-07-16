package az.manga.demo.usercontent.history.internal;

import az.manga.demo.usercontent.history.api.dto.HistoryDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface HistoryMapper {

    @Mapping(target = "manga", ignore = true)
    @Mapping(target = "chapter",  ignore = true)
    HistoryDto toDto(ReadingHistory history);

}
