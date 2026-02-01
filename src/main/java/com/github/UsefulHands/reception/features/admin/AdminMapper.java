package com.github.UsefulHands.reception.features.admin;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AdminMapper {

    @Mapping(source = "user.id", target = "userId")
    AdminDto toDto(AdminEntity entity);

    @Mapping(target = "user", ignore = true) // User serviste manuel setlenecek
    @Mapping(target = "id", ignore = true)
    AdminEntity toEntity(AdminDto dto);
}