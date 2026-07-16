package az.manga.demo.usercontent.rating.internal;

import az.manga.demo.usercontent.rating.internal.dto.RatingCreateDto;
import az.manga.demo.usercontent.rating.api.dto.RatingDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RatingMapper {

    RatingDto toDto(Rating rating);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Rating toEntity(RatingCreateDto request);


}
