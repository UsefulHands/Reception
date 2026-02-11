package com.github.UsefulHands.reception.features.reservation;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ReservationMapper {

    @Mapping(target = "room.id", source = "roomId")
    @Mapping(target = "guest.id", source = "guestId")
    ReservationEntity toEntity(ReservationDto dto);

    @Mapping(target = "roomId", source = "room.id")
    @Mapping(target = "guestId", source = "guest.id")
    @Mapping(target = "roomNumber", source = "room.roomNumber")
    @Mapping(target = "guestFirstName", source = "guest.firstName")
    @Mapping(target = "guestLastName", source = "guest.lastName")
    @Mapping(target = "phoneNumber", source = "guest.phoneNumber")
    @Mapping(target = "identityNumber", source = "guest.identityNumber")
    @Mapping(target = "guestFullName", expression = "java(entity.getGuest().getFirstName() + \" \" + entity.getGuest().getLastName())")
    @Mapping(target = "balance", expression = "java(entity.getTotalPrice().subtract(entity.getAmountPaid() != null ? entity.getAmountPaid() : java.math.BigDecimal.ZERO))")
    ReservationDto toDto(ReservationEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "room", ignore = true) // Service içinde manuel set ediyoruz
    @Mapping(target = "guest", ignore = true)
    void updateEntityFromDto(ReservationDto dto, @MappingTarget ReservationEntity entity);
}
