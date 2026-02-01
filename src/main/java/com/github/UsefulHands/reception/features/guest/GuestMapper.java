package com.github.UsefulHands.reception.features.guest;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GuestMapper {
    @Mapping(source = "user.id", target = "userId")
    GuestDto toDto(GuestEntity entity);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "id", ignore = true)
    GuestEntity toEntity(GuestDto dto);
}