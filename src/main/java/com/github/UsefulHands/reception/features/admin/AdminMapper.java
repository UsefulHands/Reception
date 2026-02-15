package com.github.UsefulHands.reception.features.admin;

import com.github.UsefulHands.reception.features.admin.dtos.AdminDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AdminMapper {

    @Mapping(source = "user.id", target = "userId")
    AdminDto toDto(AdminEntity entity);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "id", ignore = true)
    AdminEntity toEntity(AdminDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    void updateEntityFromDto(AdminDto adminDto, @MappingTarget AdminEntity adminEntity);
}