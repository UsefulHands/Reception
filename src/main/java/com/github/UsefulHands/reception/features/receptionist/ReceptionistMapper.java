package com.github.UsefulHands.reception.features.receptionist;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReceptionistMapper {

    @Mapping(source = "user.id", target = "userId")
    ReceptionistDto toDto(ReceptionistEntity entity);

    @Mapping(target = "user", ignore = true) // User serviste manuel setlenecek
    @Mapping(target = "id", ignore = true)
    ReceptionistEntity toEntity(ReceptionistDto dto);
}