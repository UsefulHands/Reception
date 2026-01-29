package com.github.UsefulHands.reception.features.guest;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface GuestMapper {
    GuestDto toDto(GuestEntity guest);
    GuestEntity toEntity(GuestDto guestDto);
}
