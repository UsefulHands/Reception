package com.github.UsefulHands.reception.features.room;

import com.github.UsefulHands.reception.features.room.dtos.RoomDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface RoomMapper {

    RoomDto toDto(RoomEntity entity);

    @Mapping(target = "id", ignore = true)
    RoomEntity toEntity(RoomDto dto);

    @Mapping(target = "id", ignore = true)
    void updateEntityFromDto(RoomDto dto, @MappingTarget RoomEntity entity);
}
