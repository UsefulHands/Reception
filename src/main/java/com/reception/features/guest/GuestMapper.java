package com.reception.features.guest;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface GuestMapper {

    GuestDto toDto(GuestEntity entity);

    GuestEntity toEntity(GuestDto dto);
}
