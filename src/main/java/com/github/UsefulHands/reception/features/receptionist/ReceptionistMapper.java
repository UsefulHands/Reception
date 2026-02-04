package com.github.UsefulHands.reception.features.receptionist;

import com.github.UsefulHands.reception.features.guest.GuestDto;
import com.github.UsefulHands.reception.features.guest.GuestEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ReceptionistMapper {

    @Mapping(source = "user.id", target = "userId")
    ReceptionistDto toDto(ReceptionistEntity entity);

    @Mapping(target = "user", ignore = true) // User serviste manuel setlenecek
    @Mapping(target = "id", ignore = true)
    ReceptionistEntity toEntity(ReceptionistDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    void updateEntityFromDto(ReceptionistDto dto, @MappingTarget ReceptionistEntity entity);
}